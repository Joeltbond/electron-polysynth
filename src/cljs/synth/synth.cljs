(ns synth.synth
  (:require [synth.audio.note-processor :as np]))

(def ctx (js/AudioContext.))
(def active-voices (atom {}))

;; utils
(defn- connect [& nodes]
  (doall
    (map (fn [[a b]] (.connect a b))
         (partition 2 1 nodes))))

;; protocols
(defprotocol IOscillator
  (set-waveform! [this waveform])
  (set-frequency! [this frequency]))

(defprotocol IGain
  (set-gain! [this gain]))

(defprotocol IConnectable
  (patch-to! [this input]))

;; entities
(defrecord LFO [oscillator gain]
  IOscillator
  (set-waveform! [this waveform]
    (set! (.-type oscillator) waveform))
  (set-frequency! [this frequency]
    (.setValueAtTime this.oscillator.frequency frequency ctx.currentTime))
  IGain
  (set-gain! [this amplitude]
    (.setValueAtTime this.gain.gain amplitude ctx.currentTime))
  IConnectable
  (patch-to! [this input] (.connect this.gain input)))

(defn make-lfo [context]
  (let [lfo (->LFO (.createOscillator context) (.createGain context))]
    (.connect (:oscillator lfo) (:gain lfo))
    (.start (:oscillator lfo))
    lfo))


;; vibrato
(def vibrato (make-lfo ctx))

;; master filter
(def master-filter (.createBiquadFilter ctx))
(set! (.-type master-filter) "lowpass")
(set! (.-value master-filter.frequency) 10000)

;; master volume
(def master-volume (.createGain ctx))
(set! (.-value master-volume.gain) 0.1)

;; routing
(connect master-filter master-volume ctx.destination)

;; conversions
(defn percent-to-freq [percent]
  (* percent 100))
(defn percent-to-q [percent]
  (/ percent 10))
(defn percent-to-lfo-speed [percent]
  (/ percent 5))
(defn percent-to-lfo-depth [percent]
  (/ percent 4))

;; controls
(defn update-frequency! [percent]
  (let [new-freq (percent-to-freq percent)]
    (set! (.-value master-filter.frequency) new-freq)))
(defn update-q![percent]
  (let [new-q (percent-to-q percent)]
    (set! (.-value master-filter.Q) new-q)))
(defn update-lfo-speed! "no conversion" [percent]
  (let [speed (percent-to-lfo-speed percent)]
    (set-frequency! vibrato speed)))
(defn update-lfo-depth! [percent]
  (let [depth (percent-to-lfo-depth percent)]
    (set-gain! vibrato depth)))

(defn- create-oscillator [frequency wave]
  (let [osc (.createOscillator ctx)]
    (set! (.-type osc) (name wave))
    (set! (.-value osc.frequency) frequency)
    osc))

(defn start-note
  "Starts a new synth voice and adds it to the map of active voices"
  [freq]
  (let [osc (create-oscillator freq "sine")
        gain (.createGain ctx)
        vibrato-tuner (.createGain ctx)]
    (.log js/console freq)
    (set! (.-value gain.gain) 0.1)
    (set! (.-value vibrato-tuner.gain) (/ freq 400))
    (patch-to! vibrato vibrato-tuner)
    (connect vibrato-tuner osc.frequency)
    (connect osc master-filter)
    (.start osc)
    (swap! active-voices assoc freq osc)))

(defn stop-note
  "Stops a synth voice and removes it from the map of active voices"
  [freq]
  (let [osc (@active-voices freq)]
    (.stop osc)
    (swap! active-voices dissoc freq)))

(def note-processor (np/make-note-processor
  {:on-note-on start-note
   :on-note-off stop-note}))

(defn listen-for-notes [channel]
  (np/listen note-processor channel))

;; use to pass in inital state
(defn init! [settings]
  (do (update-frequency! (settings :filter-frequency))
    (update-q! (settings :filter-q))
    (update-lfo-speed! (settings :lfo-speed))
    (update-lfo-depth! (settings :lfo-depth))))

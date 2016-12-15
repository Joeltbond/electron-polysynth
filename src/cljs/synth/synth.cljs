(ns synth.synth
  (:require [synth.audio.note-processor :as np]
            [synth.audio.envelope :as env]
            [synth.audio.voice :as voice]
            [synth.audio.utils :as utils]))

(def ctx (js/AudioContext.))
(def active-voices (atom {}))
(def current-osc-wave (atom :sine))
(def adsr (atom {:attack-time 0 :decay-time 0 :sustain-level 1 :release-time 0}))
; (def voice-bank (vector (repeat (voice/make-voice ctx) 8)))

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
(set! (.-value master-volume.gain) 0.5)

;; routing
(utils/connect master-filter master-volume ctx.destination)

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
;; TODO: Fix bad api. switch to key-based
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
(defn update-osc-wave! [wave]
  (reset! current-osc-wave wave))
(defn update-attack! [percent]
  (swap! adsr assoc :attack-time (/ percent 10)))
(defn update-decay! [percent]
  (swap! adsr assoc :decay-time (/ percent 10)))
(defn update-release! [percent]
  (swap! adsr assoc :release-time (/ percent 10)))
(defn update-sustain! [percent]
  (swap! adsr assoc :sustain-level (/ percent 100)))

;;TODO. need to delete nodes
(defn start-note
  "Starts a new synth voice and adds it to the map of active voices"
  [freq]
  (let [v (voice/make-voice ctx (:gain vibrato) master-filter)]
    (voice/trigger-on ctx v freq @current-osc-wave @adsr)
    (swap! active-voices assoc freq v)))

(defn stop-note
  "Stops a synth voice and removes it from the map of active voices"
  [freq]
  (let [voice (@active-voices freq)]
    (voice/trigger-off ctx voice @adsr)
    (swap! active-voices dissoc freq)))

(def note-processor (np/make-note-processor
  {:on-note-on start-note
   :on-note-off stop-note}))

(defn listen-for-notes [channel]
  (np/listen note-processor channel))

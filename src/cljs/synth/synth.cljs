(ns synth.synth
  (:require [synth.audio.note-processor :as np]
            [synth.audio.voice :as voice]
            [synth.audio.utils :as utils]))

(def waves [:sawtooth :square :triangle :sine])

(def ctx (js/AudioContext.))
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

;; entities TODO: use plain helper functions instead of lfo abstraction
(defrecord LFO [oscillator gain]
  IOscillator
  (set-waveform! [this waveform]
    (utils/set-type! this.oscillator waveform))
  (set-frequency! [this frequency]
    (utils/set-frequency! this.oscillator frequency))
  IGain
  (set-gain! [this amplitude]
    (utils/set-gain! this.gain amplitude))
  IConnectable
  (patch-to! [this input] (utils/connect this.gain input)))

(defn make-lfo [context]
  (let [lfo (->LFO (utils/make-oscillator ctx) (utils/make-gain context))]
    (utils/connect (:oscillator lfo) (:gain lfo))
    (utils/start-osc (:oscillator lfo))
    lfo))

;; vibrato
(def vibrato (make-lfo ctx))

;; master filter
(def master-filter (utils/make-biquad-filter ctx))
(utils/set-type! master-filter "lowpass")
(utils/set-frequency! master-filter 10000)

(def active-voices (atom {}))
(def free-voices 
  (->> (repeatedly #(voice/make-voice ctx (:gain vibrato) master-filter))
    (take 8)
    (atom)))

;; master volume
(def master-volume (utils/make-gain ctx))
(utils/set-gain! master-volume 0.5)

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
    (utils/set-frequency! master-filter new-freq)))
(defn update-q![percent]
  (let [new-q (percent-to-q percent)]
    (utils/set-q! master-filter new-q)))
(defn update-lfo-speed! [percent]
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
(defn start-note [freq]
  (let [v (first @free-voices)]
    (when v
      (voice/trigger-on ctx v freq @current-osc-wave @adsr)
      (swap! free-voices rest)
      (swap! active-voices assoc freq v))))

(defn stop-note [freq]
  (let [v (@active-voices freq)]
    (when v
      (voice/trigger-off ctx v @adsr)
      (swap! free-voices conj v)
      (swap! active-voices dissoc freq))))

(def note-processor (np/make-note-processor
  {:on-note-on start-note
   :on-note-off stop-note}))

(defn listen-for-notes [channel]
  (np/listen note-processor channel))

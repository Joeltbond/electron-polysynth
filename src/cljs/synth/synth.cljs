(ns synth.synth
  (:require [synth.audio.note-processor :as np]
            [synth.audio.voice :as voice]
            [synth.audio.utils :as utils]))

(def waves [:sawtooth :square :triangle :sine])
(def ctx (js/AudioContext.))
(def current-osc-wave (atom))
(def adsr (atom {:attack-time 0 :decay-time 0 :sustain-level 1 :release-time 0}))

;; vibrato
(def vibrato-osc (utils/make-oscillator ctx))
(def vibrato-amp (utils/make-gain ctx))
(utils/connect vibrato-osc vibrato-amp)
(utils/start-osc vibrato-osc)

;; master filter
(def master-filter (utils/make-biquad-filter ctx))
(utils/set-type! master-filter "lowpass")
(utils/set-frequency! master-filter 10000)

(def active-voices (atom {}))
(def free-voices 
  (->> (repeatedly #(voice/make-voice ctx vibrato-amp master-filter))
    (take 8)
    (atom)))

;; master volume
(def master-volume (utils/make-gain ctx))
(utils/set-gain! master-volume 0.5)

;; main routing
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
(defmulti update-param! (fn [key value] key))

(defmethod update-param! :filter-freq [_ percent]
  (let [new-freq (percent-to-freq percent)]
    (utils/set-frequency! master-filter new-freq)))

(defmethod update-param! :filter-q [_ percent]
  (let [new-q (percent-to-q percent)]
    (utils/set-q! master-filter new-q)))

(defmethod update-param! :vibrato-speed [_ percent]
  (let [speed (percent-to-lfo-speed percent)]
    (utils/set-frequency! vibrato-osc speed)))

(defmethod update-param! :vibrato-depth [_ percent]
  (let [depth (percent-to-lfo-depth percent)]
    (utils/set-gain! vibrato-amp depth)))

(defmethod update-param! :waveform [_ wave]
  (reset! current-osc-wave wave))

(defmethod update-param! :attack-time [_ percent]
  (swap! adsr assoc :attack-time (/ percent 10)))

(defmethod update-param! :decay-time [_ percent]
  (swap! adsr assoc :decay-time (/ percent 10)))

(defmethod update-param! :sustain-level [_ percent]
  (swap! adsr assoc :sustain-level (/ percent 100)))

(defmethod update-param! :release-time [_ percent]
  (swap! adsr assoc :release-time (/ percent 10)))

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

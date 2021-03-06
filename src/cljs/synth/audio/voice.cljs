(ns synth.audio.voice
	(:require [synth.audio.utils :as utils]))

(defrecord Voice
	[oscillator vca vibrato-amplitude])

(defn make-voice [context pitch-lfo output]
	(let [osc (utils/make-oscillator context)
		    vca (utils/make-gain context)
		    vibrato-amplitude (utils/make-gain context)
		    now (.-currentTime context)]

		(utils/connect pitch-lfo vibrato-amplitude osc.frequency)
		(utils/connect osc vca output)
		(utils/start-osc osc)
		(.setValueAtTime vca.gain 0 now)
		(->Voice osc vca vibrato-amplitude)))

(defn trigger-on [context voice frequency wave adsr]
	(let [osc (:oscillator voice)
				vibrato-amplitude (:vibrato-amplitude voice)
				vca-gain (.-gain (:vca voice))
				attack (:attack-time adsr)
				decay (:decay-time adsr)
				sustain (:sustain-level adsr)
		    now (.-currentTime context)]

		(utils/set-gain! vibrato-amplitude (/ frequency 400))
		(prn wave)
		(utils/set-type! osc (name wave))
		(utils/set-frequency! osc frequency)

		(.cancelScheduledValues vca-gain now)
		(.setValueAtTime vca-gain 0 now)
		(.linearRampToValueAtTime vca-gain 1 (+ now attack))
		(.linearRampToValueAtTime vca-gain sustain (+ now attack decay))))

(defn trigger-off [context voice adsr]
	(let [vca-gain (.-gain (:vca voice))
				release (:release-time adsr)
				now (.-currentTime context)]

		(.cancelScheduledValues vca-gain now)
		(.linearRampToValueAtTime vca-gain 0 (+ now release))))


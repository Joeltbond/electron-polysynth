(ns synth.audio.voice)

(defrecord Voice [oscillator envelope])

(defn make-voice [oscillator envelope]
	(->Voice oscillator envelope))

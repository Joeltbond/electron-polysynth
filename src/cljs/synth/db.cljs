(ns synth.db
	(:require [cljs.spec :as s]))

(s/def ::vibrato-speed number?)
(s/def ::vibrato-depth number?)

(s/def ::filter-q number?)
(s/def ::filter-freq number?)

(s/def ::waveform keyword?)

(s/def ::attack-time number?)
(s/def ::decay-time number?)
(s/def ::sustain-level number?)
(s/def ::release-time number?)

(s/def ::db (s/keys :req-un [::vibrato-speed
														 ::vibrato-depth
														 ::filter-q
														 ::filter-freq
														 ::waveform
														 ::attack-time
														 ::decay-time
														 ::sustain-level
														 ::release-time]))

(def default-value 
	{:vibrato-speed 10
	 :vibrato-depth 8
	 :filter-q 50
	 :filter-freq 10
	 :waveform :sawtooth
	 :attack-time 10
	 :decay-time 50
	 :sustain-level 100
	 :release-time 50})

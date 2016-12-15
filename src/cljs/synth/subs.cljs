(ns synth.subs
	(:require [re-frame.core :refer [reg-sub subscribe]]))

;; TODO: this can't be right...

(reg-sub
  :vibrato-speed
  (fn [db _] 
    (:vibrato-speed db)))
(reg-sub
  :vibrato-depth
  (fn [db _] 
    (:vibrato-depth db)))

(reg-sub
  :filter-q
  (fn [db _] 
    (:filter-q db)))
(reg-sub
  :filter-freq
  (fn [db _] 
    (:filter-freq db)))

(reg-sub
  :attack-time
  (fn [db _] 
    (:attack-time db)))
(reg-sub
  :decay-time
  (fn [db _] 
    (:decay-time db)))
(reg-sub
  :sustain-level
  (fn [db _] 
    (:sustain-level db)))
(reg-sub
  :release-time
  (fn [db _] 
    (:release-time db)))

(reg-sub
  :waveform
  (fn [db _] 
    (:waveform db)))

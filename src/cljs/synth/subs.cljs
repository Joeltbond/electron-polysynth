(ns synth.subs
	(:require [re-frame.core :refer [reg-sub subscribe]]))

(defn query [db [event-id]]
  (event-id db))

(reg-sub :vibrato-speed query)
(reg-sub :vibrato-depth query)
(reg-sub :filter-q query)
(reg-sub :filter-freq query)
(reg-sub :attack-time query)
(reg-sub :decay-time query)
(reg-sub :sustain-level query)
(reg-sub :release-time query)
(reg-sub :waveform query)

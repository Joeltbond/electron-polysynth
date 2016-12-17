(ns synth.events
  (:require
    [synth.db      :refer [default-value]]
    [synth.synth   :as synth]
    [re-frame.core :refer [reg-event-db reg-fx reg-event-fx inject-cofx path trim-v
                           after debug]]
    [cljs.spec     :as s]))

(defn- check-and-throw
  "throw an exception if db doesn't match the spec."
  [a-spec db]
  (when-not (s/valid? a-spec db)
    (throw (ex-info (str "spec check failed: " (s/explain-str a-spec db)) {}))))

(def check-spec-interceptor (after (partial check-and-throw :synth.db/db)))

(defn- rectify-knob-value [value delta]
	(let [new-value (+ value delta)]
		(cond
			(> new-value 100) 100
			(< new-value 0) 0
			:else new-value)))

(reg-fx
	:initialise-synth
	(fn [val]
		(doseq [[k v] val]
			(synth/update-param! k v))))

(reg-fx
	:update-synth
	(fn [val]
		(prn val)
		(synth/update-param! (:key val) (:value val))))

(reg-event-fx
  :initialise
  [check-spec-interceptor]
  (fn [world _]
    {:db (merge (:db world) default-value)
     :initialise-synth default-value})) 

(reg-event-fx
	:knob-scroll
	[check-spec-interceptor trim-v]
	(fn [world [key delta]]
		(let [db (:db world)
					rectified-value (rectify-knob-value (key db) delta)]
			{:db (assoc db key rectified-value)
			 :update-synth {:key key
			 	              :value rectified-value}})))

(reg-event-fx
	:change-waveform
	[check-spec-interceptor trim-v]
	(fn [world [new-wv]]
		(let [db (:db world)]
			(prn "new-wv")
			(prn new-wv)
			{:db (assoc db :waveform new-wv)
			 :update-synth {:key :waveform
			 	              :value new-wv}})))

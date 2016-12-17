(ns synth.events
  (:require
    [synth.db      :refer [default-value]]
    [synth.synth   :as synth]
    [re-frame.core :refer [reg-event-db reg-event-fx inject-cofx path trim-v
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

;; TODO: only update changed param
(defn- sync-synth [db]
	(synth/update-param! :filter-freq (:filter-freq db))
	(synth/update-param! :filter-q (:filter-q db))
	(synth/update-param! :vibrato-speed (:vibrato-speed db))
	(synth/update-param! :vibrato-depth (:vibrato-depth db))
	(synth/update-param! :waveform (:waveform db))
	(synth/update-param! :attack-time (:attack-time db))
	(synth/update-param! :decay-time (:decay-time db))
	(synth/update-param! :sustain-level (:sustain-level db))
	(synth/update-param! :release-time (:release-time db)))

(def sync-synth-interceptor (after sync-synth))

(reg-event-db
  :initialise-db
  [check-spec-interceptor sync-synth-interceptor]
  (fn
  	[db _]
    (merge db default-value))) 

(reg-event-db
	:knob-scroll
	[check-spec-interceptor sync-synth-interceptor trim-v]
	(fn [db [key delta]]
		(let [new-value (rectify-knob-value (key db) delta)]
			(assoc db key new-value))))

(reg-event-db
	:change-waveform
	[check-spec-interceptor sync-synth-interceptor (path :waveform) trim-v]
	(fn [old-wf [new-wv]]
		new-wv))
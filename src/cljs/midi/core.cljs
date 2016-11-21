(ns midi.core
  (:require [midi.synth :as s]
            [reagent.core :as r]
            [cljsjs.react]))

(def app-state (r/atom
	{:message "waiting for notes.."}))

(defn get-frequency [note]
  (* 440 (.pow js/Math 2 (/ (- note 69) 12))))

(defn on-midi-message [message]
  (let [data (.-data message)]
    (swap! app-state assoc :message data)
    (if (= (aget data 0) 144)
      (s/start-note (get-frequency (aget data 1)))
      (s/stop-note (get-frequency (aget data 1))))))

(defn attach-midi-handlers [inputs]
  (loop [input (.next inputs)]
    (if (not (.-done input))
      (do (set! (.-onmidimessage input.value) on-midi-message)
        (recur (.next inputs))))))

(defn on-midi-success [midi-access]
  (do (.log js/console "got midi access")
      (attach-midi-handlers (.values midi-access.inputs))))

(defn on-midi-failure []
	(.log js/console "failed to get midi access"))

(defn request-midi-access []
	(let [midi-promise (.requestMIDIAccess js/navigator (js-obj "sysex" false))]
		(.then midi-promise on-midi-success on-midi-failure)))

(defn main-page
  []
  [:div (@app-state :message)])

(defn mount-root
  []
  (r/render [main-page] (.getElementById js/document "app")))

(defn init!
  []
  (do (request-midi-access)
  	  (mount-root)))

(ns synth.midi
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [put! >! chan]]))

(def midi-events (chan 100))

(defn- get-frequency [note]
  (* 440 (.pow js/Math 2 (/ (- note 69) 12))))

(defn- on-midi-message [message]
  (let [note-number (aget (.-data message) 1)
	      message-number (aget (.-data message) 0)
        message-type (case message-number 144 :note-on 128 :note-off :other)]
    (prn note-number)
    (prn message-number)
    (prn message-type)
    (go (>! midi-events {:event-type message-type :frequency (get-frequency note-number)}))))

(defn- attach-midi-handlers [inputs]
  (loop [input (.next inputs)]
    (if (not (.-done input))
      (do (set! (.-onmidimessage input.value) on-midi-message)
        (recur (.next inputs))))))

(defn- on-midi-success [midi-access]
  (do (.log js/console "got midi access")
      (attach-midi-handlers (.values midi-access.inputs))))

(defn- on-midi-failure []
	(.log js/console "failed to get midi access"))

(defn- request-midi-access []
	(let [midi-promise (.requestMIDIAccess js/navigator (js-obj "sysex" false))]
		(.then midi-promise on-midi-success on-midi-failure)))

;; API
(defn get-output [] midi-events)

;; dev helper button
(defn on-key-press []
  (on-midi-message (js-obj "data" (js/Array 144 57))))

(defn on-key-depress []
  (on-midi-message (js-obj "data" (js/Array 128 57))))

(.addEventListener (.getElementById js/document "button") "mousedown" on-key-press)
(.addEventListener (.getElementById js/document "button") "mouseup" on-key-depress)

;; startup
(request-midi-access)
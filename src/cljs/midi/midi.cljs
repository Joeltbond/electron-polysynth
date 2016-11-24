(ns midi.midi)

(def callbacks (atom {:on-keydown nil :on-keyup nil}))

(defn get-frequency [note]
  (* 440 (.pow js/Math 2 (/ (- note 69) 12))))

;todo be specific about events
(defn on-midi-message [message]
  (let [data (.-data message)]
    (if (= (aget data 0) 144)
      ((@callbacks :on-keydown) (get-frequency (aget data 1)))
      ((@callbacks :on-keyup) (get-frequency (aget data 1))))))

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

(defn init! [on-keydown on-keyup]
	(swap! callbacks (fn [old new] new) {:on-keydown on-keydown :on-keyup on-keyup})
	(request-midi-access))
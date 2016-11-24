(ns midi.synth)

(def ctx (js/AudioContext.))

(def gain (.createGain ctx))
(set! (.-value gain.gain) 0.1)
(.connect gain ctx.destination)

(def vib (.createOscillator ctx))
(def vib-amount (.createGain ctx))
(set! (.-type vib) "sine")
(set! (.-value vib.frequency) 2)
(set! (.-value vib-amount.gain) 3)
(.connect vib vib-amount)
(.start vib)

(def synth-filter (.createBiquadFilter ctx))
(set! (.-type synth-filter) "lowpass")
(set! (.-value synth-filter.frequency) 10000)
(.connect synth-filter gain)

(def oscillators (atom {}))
(def wave (atom "sine"))

(defn update-wave! [new-wave] (reset! wave new-wave))
(defn update-frequency! [new-freq]
    (set! (.-value synth-filter.frequency) new-freq))
(defn update-q! [new-q]
    (set! (.-value synth-filter.Q) new-q))

(defn start-note [freq]
	(let [osc (.createOscillator ctx)
        gain (.createGain ctx)]
		(set! (.-type osc) @wave)
		(set! (.-value osc.frequency) freq)
        (.connect osc synth-filter)
    (.connect gain ctx.destination)
    (.connect vib-amount osc.frequency)
    (.start osc)
		(swap! oscillators assoc freq osc)))

(defn stop-note [freq]
  (let [osc (@oscillators freq)]
    (.stop osc)
    (swap! oscillators dissoc freq)))
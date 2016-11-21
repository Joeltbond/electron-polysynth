(ns midi.synth)

(def ctx (js/AudioContext.))

(def gain (.createGain ctx))
(set! (.-value gain.gain) 0.1)
(.connect gain ctx.destination)

(def vib (.createOscillator ctx))
(def vib-amount (.createGain ctx))
(set! (.-type vib) "sine")
(set! (.-value vib.frequency) 1.3)
(set! (.-value vib-amount.gain) 2)
(.connect vib vib-amount)
(.start vib)

(def oscillators (atom {}))

(defn start-note [freq]
	(let [osc (.createOscillator ctx)
        gain (.createGain ctx)]
		(set! (.-type osc) "sine")
		(set! (.-value osc.frequency) freq)
		(.connect osc gain)
    (.connect gain ctx.destination)
    (.connect vib-amount osc.frequency)
    (.start osc)
		(swap! oscillators assoc freq osc)))

(defn stop-note [freq]
  (let [osc (@oscillators freq)]
    (.stop osc)
    (swap! oscillators dissoc freq)))
(ns midi.synth)

(def waves [:sine :square :sawtooth :triangle])

(def ctx (js/AudioContext.))

(def gain (.createGain ctx))
(set! (.-value gain.gain) 0.1)
(.connect gain ctx.destination)

(def vib (.createOscillator ctx))
(def vib-gain (.createGain ctx))
(set! (.-type vib) "sine")
(set! (.-value vib.frequency) 2)
(set! (.-value vib-gain.gain) 3)
(.connect vib vib-gain)
(.start vib)

(def synth-filter (.createBiquadFilter ctx))
(set! (.-type synth-filter) "lowpass")
(set! (.-value synth-filter.frequency) 10000)
(.connect synth-filter gain)

(def oscillators (atom {}))

(defn get-waves [] waves)

(defn update-frequency! [new-freq]
    (set! (.-value synth-filter.frequency) new-freq))
(defn update-q! [new-q]
    (set! (.-value synth-filter.Q) new-q))
(defn update-lfo-speed! [new-speed]
    (set! (.-value vib.frequency) new-speed))
(defn update-lfo-depth! [new-depth]
    (set! (.-value vib-gain.gain) new-depth))

(defn start-note [freq wave]
	(let [osc (.createOscillator ctx)
        gain (.createGain ctx)]
		(set! (.-type osc) (name wave))
		(set! (.-value osc.frequency) freq)
        (.connect osc synth-filter)
    (.connect gain ctx.destination)
    (.connect vib-gain osc.frequency)
    (.start osc)
		(swap! oscillators assoc freq osc)))

(defn stop-note [freq]
  (let [osc (@oscillators freq)]
    (.stop osc)
    (swap! oscillators dissoc freq)))
(ns synth.audio.utils)

(defn connect [& nodes]
  (doall
    (map (fn [[a b]] (.connect a b))
         (partition 2 1 nodes))))

(defn make-oscillator [ctx]
  (.createOscillator ctx))

(defn make-gain [ctx]
	(.createGain ctx))

(defn make-biquad-filter [ctx]
	(.createBiquadFilter ctx))

(defn set-frequency! [audio-node value]
	(set! (.-value audio-node.frequency) value))

(defn set-type! [audio-node type]
	(set! (.-type audio-node) type))

(defn set-gain! [audio-node value]
	(set! (.-value audio-node.gain) value))

(defn set-q! [audio-node value]
	(set! (.-value audio-node.Q) value))

(defn start-osc [osc]
	(.start osc))
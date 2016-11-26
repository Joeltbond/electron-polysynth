(ns synth.synth)

(def ctx (js/AudioContext.))
(def active-voices (atom {}))

;; vibrato
(def vib (.createOscillator ctx))
(def vib-gain (.createGain ctx))
(set! (.-type vib) "sine")
(set! (.-value vib.frequency) 2)
(set! (.-value vib-gain.gain) 3)
(.connect vib vib-gain)
(.start vib)

;; master filter
(def master-filter (.createBiquadFilter ctx))
(set! (.-type master-filter) "lowpass")
(set! (.-value master-filter.frequency) 10000)

;; master volume
(def master-volume (.createGain ctx))
(set! (.-value master-volume.gain) 0.1)

;; routing
(.connect master-filter master-volume)
(.connect master-volume ctx.destination)

;; conversions
(defn percent-to-freq [percent]
  (* percent 100))
(defn percent-to-q [percent]
  (/ percent 10))
(defn percent-to-lfo-speed [percent]
  (/ percent 5))
(defn percent-to-lfo-depth [percent]
  (/ percent 4))

;; controls
(defn update-frequency! [percent]
  (let [new-freq (percent-to-freq percent)]
    (set! (.-value master-filter.frequency) new-freq)))
(defn update-q![percent]
  (let [new-q (percent-to-q percent)]
    (set! (.-value master-filter.Q) new-q)))
(defn update-lfo-speed! "no conversion" [percent]
  (let [new-speed (percent-to-lfo-speed percent)]
    (set! (.-value vib.frequency) new-speed)))
(defn update-lfo-depth! [percent]
  (let [new-depth (percent-to-lfo-depth percent)]
    (set! (.-value vib-gain.gain) new-depth)))

(defn start-note
  "Starts a new synth voice and adds it to the map of active voices"
  [freq wave]
  (let [osc (.createOscillator ctx)
        gain (.createGain ctx)]
    (set! (.-type osc) (name wave))
    (set! (.-value osc.frequency) freq)
    (set! (.-value gain.gain) 0.1)
    (.connect vib-gain osc.frequency)
    (.connect osc master-filter)
    (.start osc)
    (swap! active-voices assoc freq osc)))

(defn stop-note
  "Stops a synth voice and removes it from the map of active voices"
  [freq]
  (let [osc (@active-voices freq)]
    (.stop osc)
    (swap! active-voices dissoc freq)))

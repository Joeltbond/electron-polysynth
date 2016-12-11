(ns synth.audio.envelope)

(defprotocol IEnvelope
	(trigger-on [this])
	(trigger-off [this]))

(defrecord Envelope [context parameter adsr]
	IEnvelope
	(trigger-on [this]
		(let [parameter (:parameter this)
					adsr (deref (:adsr this))
					attack (:attack-time adsr)
					decay (:decay-time adsr)
					sustain (:sustain-level adsr)
			    now (.-currentTime (:context this))]
			(.cancelScheduledValues parameter now)
			(.setValueAtTime parameter 0 now)
			(.linearRampToValueAtTime parameter 1 (+ now attack))
			(.exponentialRampToValueAtTime parameter sustain (+ now attack decay))))

	(trigger-off [this]
		(let [parameter (:parameter this)
					adsr (deref (:adsr this))
					release (:release-time adsr)
					now (.-currentTime (:context this))]
			(.cancelScheduledValues parameter now)
			(.linearRampToValueAtTime parameter 0 (+ now release)))))


(defn make-envelope [context parameter adsr]
	(->Envelope context parameter (atom adsr)))
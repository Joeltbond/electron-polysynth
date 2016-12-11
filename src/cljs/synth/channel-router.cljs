(ns synth.channel-router
	(:require [synth.synth :as synth]
		[synth.midi :as midi]
		[cljs.core.async :refer [>! chan]]))

(defn init! []
	(synth/listen-for-notes (midi/get-output)))
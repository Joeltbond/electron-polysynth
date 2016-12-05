(ns synth.core
  (:require [synth.midi :as m]
            [synth.synth :as s]
            [synth.components :as c]
            [synth.ui :as ui]
            [synth.channel-router :as cr]
            [cljsjs.react]))

(defn mount-root
  []
  (do (cr/init!)
    (ui/init!)))

(defn init!
  []
  (mount-root))

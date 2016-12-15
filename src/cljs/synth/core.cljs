(ns synth.core
  (:require [synth.views]
            [reagent.core :as reagent]
            [synth.events]
            [synth.subs]
            [synth.channel-router :as channel-router]
            [re-frame.core :refer [dispatch dispatch-sync]]
            [synth.channel-router :as cr]))


(defn mount-root
  []
  (dispatch-sync [:initialise-db])
  (reagent/render [synth.views/main-page]
                  (.getElementById js/document "app")))
  (channel-router/init!)

(defn init!
  []
  (mount-root))

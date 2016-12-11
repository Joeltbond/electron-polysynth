(ns synth.core
  (:require [synth.views]
            [reagent.core :as reagent]
            [synth.events]
            [synth.subs]
            [re-frame.core :refer [dispatch dispatch-sync]]
            [synth.channel-router :as cr]))

(enable-console-print!)

(defn mount-root
  []
  (dispatch-sync [:initialise-db])
  (reagent/render [synth.views/main-page]
                  (.getElementById js/document "app")))

(defn init!
  []
  (mount-root))

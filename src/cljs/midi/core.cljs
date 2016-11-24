(ns midi.core
  (:require [midi.midi :as m]
            [midi.synth :as s]
            [reagent.core :as r]
            [cljsjs.react]))

(def waves [:sine :square :sawtooth :triangle])

(defn wave-button
  [wave]
  [:button {:on-click #(s/update-wave! (name wave))}
    (name wave)])

(defn q-slider
  []
  [:div
    [:input {:type "range" :min 0 :max 30
     :on-change #(s/update-q! (-> % .-target .-value))}]])

(defn frequency-slider
  []
  [:div
    [:input {:type "range" :min 0 :max 30000
     :on-change #(s/update-frequency! (-> % .-target .-value))}]])

(defn main-page
  []
  [:div
    [q-slider]
    [frequency-slider]
    (for [w waves]
        [wave-button w])])

(defn mount-root
  []
  (r/render [main-page] (.getElementById js/document "app")))

(defn init!
  []
  (do (m/request-midi-access)
  	  (mount-root)))

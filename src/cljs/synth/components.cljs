(ns synth.components
	(:require [reagent.core :as r]))

(defn percent-to-deg [percent]
  (- (* percent 2.7) 135))

(defn rotate [deg]
  (str "rotate(" deg "deg)"))

(defn get-new-value [value change]
  (let [new-value (+ value (/ change 4))]
    (cond
      (< new-value 0) 0
      (> new-value 100) 100
      :else new-value)))

(defn handle-knob-scroll [current-value event callback]
  (do (.preventDefault event)
    (callback (get-new-value current-value (.-deltaY event)))))

(defn knob
  [label value on-new-value]
  [:div {:class "knob-container"}
    [:div {:class "knob" :on-wheel #(handle-knob-scroll value % on-new-value)
           :style {:transform  (rotate (percent-to-deg value))}}]
    [:div {:class "knob-label"} label]])


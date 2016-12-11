(ns synth.components
	(:require [re-frame.core :refer [subscribe dispatch]]))

(defn- percent-to-deg [percent]
  (- (* percent 2.7) 135))

(defn knob
  [label key]
  (let [value (subscribe [key])
        rotate-string #(str "rotate(" (percent-to-deg %) "deg)")]
    (fn [label]
      [:div {:class "knob-container"}
        [:div {:class "knob"
               :style {:transform (rotate-string @value)}
               :on-wheel (fn [event]
                           (.preventDefault event) 
                           (dispatch [:knob-scroll key (.-deltaY event)]))}]
        [:div {:class "knob-label"} label]])))

(defn wave-button
  [this-wave]
  (let [current-wave (subscribe [:waveform])
        on-click #(dispatch [:change-waveform this-wave])]
    (fn [this-wave]
      [:button {:on-click on-click
                :class (if (= @current-wave this-wave) "active" nil)}
       (name this-wave)])))
(ns synth.ui
  (:require [synth.synth :as s]
            [synth.components :as c]
            [reagent.core :as r]
            [cljsjs.react]))

(defonce app-state
  (r/atom
    {:wave :sawtooth
     :filter-frequency 10
     :filter-q 50
     :knob-value 100
     :lfo-speed 10
     :lfo-depth 8
     :attack 0
     :decay 50
     :sustain 100
     :release 0}))

(def waves [:sawtooth :square :triangle :sine])

;; todo: move to components ns
(defn wave-button
  [wave]
  [:button
    {:on-click #(do (swap! app-state assoc :wave wave)
                    (s/update-osc-wave! wave))
     :class (if (= (@app-state :wave) wave) "active" nil)}
    (name wave)])

(defn make-knob-callback [keyw callback]
  #(do (swap! app-state assoc keyw %)
      (callback %)))

(defn main-page
  []
  [:div {:class "app"}
    [:h1 "electron polysynth"]
    [:div
      [c/knob "lfo-speed"
              (@app-state :lfo-speed)
              (make-knob-callback :lfo-speed s/update-lfo-speed!)]
      [c/knob "lfo-depth"
              (@app-state :lfo-depth)
              (make-knob-callback :lfo-depth s/update-lfo-depth!)]
      [c/knob "filter-q"
              (@app-state :filter-q)
              (make-knob-callback :filter-q s/update-q!)]
      [c/knob "filter-freq"
              (@app-state :filter-frequency)
              (make-knob-callback :filter-frequency s/update-frequency!)]]
    [:div {:class "button-container"}
      (for [w waves]
          ^{:key w}[wave-button w])
      [:div "osc waveforms"]]
    [:div
      [c/knob "attack"
              (@app-state :attack)
              (make-knob-callback :attack #())]
      [c/knob "decay"
              (@app-state :decay #())]
      [c/knob "sustain"
              (@app-state :sustain #())]
      [c/knob "release"
              (@app-state :release #())]]])

(defn init!
  []
  (do (s/init! @app-state)
    (r/render [main-page] (.getElementById js/document "app"))))


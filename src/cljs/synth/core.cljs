(ns synth.core
  (:require [synth.midi :as m]
            [synth.synth :as s]
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
     :lfo-depth 8}))

(def waves [:sawtooth :square :triangle])

(defn start-note [freq] (s/start-note freq (@app-state :wave)))
(defn stop-note [freq] (s/stop-note freq))

;; todo: move to components ns
(defn wave-button
  [wave]
  [:button
    {:on-click #(swap! app-state assoc :wave wave)
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
      [:div "osc waveforms"]]])

(defn mount-root
  []
  (r/render [main-page] (.getElementById js/document "app")))

(defn init!
  []
  (do (m/init! start-note stop-note)
    (s/init! @app-state)
  	(mount-root)))

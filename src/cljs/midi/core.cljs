(ns midi.core
  (:require [midi.midi :as m]
            [midi.synth :as s]
            [reagent.core :as r]
            [cljsjs.react]))

(def app-state
  (r/atom
    {:wave :sine}))

(defn start-note [freq] (s/start-note freq (@app-state :wave)))
(defn stop-note [freq] (s/stop-note freq))

(defn slider
  [title min max on-change]
  [:div
    title
    [:input {:type "range" :min min :max max
     :on-change #(on-change (-> % .-target .-value))}]])

(defn wave-button
  [wave]
  [:button
    {:on-click #(swap! app-state assoc :wave wave)
     :class (if (= (@app-state :wave) wave) "active" nil)}
    (name wave)])

(defn main-page
  []
  [:div
    [slider "filter-q" 0 10 s/update-q!]
    [slider "filter-freq" 0 10000 s/update-frequency!]
    [slider "lfo-speed" 1 100 s/update-lfo-speed!]
    [slider "lfo-depth" 0 20 s/update-lfo-depth!]
    (for [w (s/get-waves)]
        ^{:key w}[wave-button w])])

(defn mount-root
  []
  (r/render [main-page] (.getElementById js/document "app")))

(defn init!
  []
  (do (m/init! start-note stop-note)
  	  (mount-root)))

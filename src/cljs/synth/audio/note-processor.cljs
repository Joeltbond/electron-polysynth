(ns synth.audio.note-processor
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [cljs.core.async :refer [take! <! chan]]))

(defprotocol INoteProcessor
  (listen [this channel]))

(defn process-note [note-processor event] 
  (case (:event-type event)
    :note-on ((:on-note-on note-processor) (:frequency event))
    :note-off ((:on-note-off note-processor) (:frequency event))
    "default"))

(defrecord NoteProcessor
  [on-note-on    ;; function to be called when a key is pressed
   on-note-off]  ;; function to be called when a key is depressed

  INoteProcessor
  (listen [this channel]
    (go-loop []
      (process-note this (<! channel))
      (recur))))

(defn make-note-processor
	[{:keys [on-note-on on-note-off]}]
  (->NoteProcessor on-note-on on-note-off))
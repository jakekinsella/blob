(ns notes.components.dialog
  (:require
    [re-frame.core :as re-frame]
    [spade.core :refer [defclass]]
    [central :as central]))

(defclass floating-style []
  {:position "absolute"
   :width "100%"
   :height "100%"
   :display "flex"
   :align-items "center"
   :justify-content "center"
   :vertical-align "middle"
   :z-index 11})
(defn floating [children] (into [:div {:class (floating-style)}] children))

(defn build [dialog]
  (if (nil? dialog)
      ()
      (floating [[:div {:on-click (fn [event] (.stopPropagation event))} "dialog"]])))

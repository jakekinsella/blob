(ns notes.components.editor
  (:require
    [reagent.core :as r]
    [re-frame.core :as re-frame]
    [react :as react]
    [spade.core :refer [defclass]]
    [central :as central]
    [notes.events :as events]
    [notes.subs :as subs]))

(defclass textbox-style []
  {:width "98%"
   :height "93%"
   :padding-left "10px"
   :padding-right "10px"
   :border "none"
   :font-size "15px"
   :font-family "'Roboto', sans-serif"
   :font-weight "100"
   :user-select "none"
   :resize "none"}
  [:&:hover {:outline "none"}])
(defn textbox [attrs]
  [:textarea (merge-with + {:class (textbox-style) :required true} attrs)])

(defn build []
  (let [selected @(re-frame/subscribe [::subs/selected])]
    [textbox {:default-value (:body selected)
              :on-change (fn [event] (re-frame/dispatch [::events/save-note (:title selected) (-> event .-target .-value)]))}]))

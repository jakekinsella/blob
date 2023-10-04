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
  [:&:focus {:outline "none"}])
(defn textbox [attrs]
  [:textarea (merge-with + {:class (textbox-style) :required true} attrs)])

(defonce title (r/atom nil))
(defonce body (r/atom nil))

(defn build []
  (let [selected @(re-frame/subscribe [::subs/selected])]
    (if (not (= (:title selected) @title)) (do (reset! title (:title selected)) (reset! body (:body selected))))
    [textbox {:value @body
              :on-change (fn [event]
                           (let [value (-> event .-target .-value)]
                             (reset! body value)
                             (re-frame/dispatch [::events/save-note @title value])))}]))

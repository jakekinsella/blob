(ns notes.components.editor
  (:require
    [re-frame.core :as re-frame]))


(defn build [selected]
  [:div "editor" [:p (:title selected)]])

(ns rehook.util)

(defn rehook-component? [e]
  (-> e meta :rehook/component true?))

(defn display-name [e]
  (if (keyword? e)
    e
    (aget e "displayName")))
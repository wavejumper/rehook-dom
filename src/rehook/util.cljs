(ns rehook.util)

(defn rehook-component? [e]
  (true? (aget e "isRehookComponent")))

(defn display-name [e]
  (if (keyword? e)
    e
    (aget e "displayName")))
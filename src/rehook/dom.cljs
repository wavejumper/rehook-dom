(ns rehook.dom
  (:require
   ["react-native" :as rn]
   ["react" :as react]))

(defmulti register-component identity)

(defmethod register-component :default [k]
  (js/console.warn "No React Native component defined for" (pr-str k))
  nil)

(defn handle-component
  [component ctx $]
  (cond
    (and (keyword? component)
         (namespace component))
    (register-component component)

    (keyword? component)
    (or (aget rn (name component))
        (register-component component))

    (aget component "isRehookComponent")
    (component ctx $)

    :else
    component))

(defn component-name
  [component]
  (if (keyword? component)
    component
    (aget component "displayName")))

(defn bootstrap
  ([ctx ctx-f elem]
   (let [ctx (ctx-f ctx elem)]
     (react/createElement (handle-component elem ctx (partial bootstrap ctx ctx-f)))))
  ([ctx ctx-f elem args]
   (let [ctx (ctx-f ctx elem)]
     (react/createElement (handle-component elem ctx (partial bootstrap ctx ctx-f)) (clj->js args))))
  ([ctx ctx-f elem args & children]
   (let [ctx (ctx-f ctx elem)]
     (apply react/createElement (handle-component elem ctx (partial bootstrap ctx ctx-f)) (clj->js args) children))))

(defn component-provider
  ([ctx component]
   (component-provider ctx identity component))
  ([ctx ctx-f component]
   (constantly #(react/createElement (bootstrap ctx ctx-f component)))))
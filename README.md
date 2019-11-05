# rehook-rn-component

[![Clojars Project](https://img.shields.io/clojars/v/wavejumper/rehook-rn-component.svg)](https://clojars.org/wavejumper/rehook-rn-component)

React Native component DSL for Clojurescript

## Rehook?

[rehook](https://github.com/wavejumper/rehook/) is a Clojurescript library for state management in React apps. 

It is a simple, 35LOC library that provides a [reagent](https://github.com/reagent-project/reagent) like interface for modern Cljs/React apps.

You do not need to use `rehook-rn-component` with `rehook`, but the two obviously pair great! 

## Basic idea

`rehook-rn-component` is designed to be: 

* A simple, hiccup-inspired DSL for templating React Native components
* A baggage-free way to pass down application context down (eg, system components) to child components

The library is only 50LOC. It makes Clojurescript development with React Native a joy!

## defui 

`defui` is a macro used to define `rehook` components. This macro is simply syntactic sugar, all `rehook` components are cljs fns.

`defui` takes in three arguments:

* `context`: immutable, application context
* `props`: any props passed to the component. This will be an untouched JS object.
* `$`: the render fn

```clojure
(defui my-component [{:keys [dispatch]} _ $] 
  ($ :Text {:onClick #(dispatch :fire-missles)} "Hello world"))
```

## $

The `$` render fn provides Hiccup-like syntax for templating. 

Its signature looks like this: 
`[component args? & children]`

* The first argument is always a component. 
* The second (optional) argument are the component props.
* The third (optional) vararg are any component children

It supports component lookup in a few ways:

* All keywords are mapped to their equivilant name in the [React Native API](https://facebook.github.io/react-native/docs/activityindicator), eg `:KeyboardAvoidingView`
* Custom React Native components (eg, those imported from npm), can be extended via the `register-component` multimethod
* All `rehook` components can be referred to directly

```clojure 
(ns example.components
  (:require 
    [rehook.dom :as dom :refer-macros [defui]]
    ["imported-react-component" :refer [ImportedReactComponent]]))

(defmethod dom/register-component :ImportedReactComponent [_]
  ImportedReactComponent)

(defui button [{:keys [dispatch]} _ $]
  ($ :Button {:title "Fire missles" :onClick #(dispatch :fire-missles)}))

(defui app [_ $]
  ($ :View {:style #js {:flex 1}}
    ($ button)
    ($ :ImportedReactComponent)))
```

Note how the `$` render fn hides having to pass the `context` map to its children through clever partial function application!

### Props gotchas

* Props passed to `$` are always converted to JS maps via `clj->js`. This fn isn't recursive, so remeber to use the `#js` literal on any nested maps!
* `rehook` does no special transformation to the keys in your props, so use `onPress` over `on-press` etc.

## Initializing

You can use the `component-provider` fn if you directly call [AppRegistry](https://facebook.github.io/react-native/docs/appregistry)

```clojure 
(ns example.core
  (:require 
    [example.components :refer [app]]
    [rehook.dom :as dom]
    ["react-native" :refer [AppRegistry]]))

(defn system []
  {:dispatch (fn [& _] (js/console.log "TODO: implement dispatch fn..."))})

(defn main []
  (.registerComponent AppRegistry "my-app" (dom/component-provider (system) app))
```

Alternatively if you don't have access to the `AppRegistry`, you can use the `boostrap` fn instead - which will return a valid React element

## Context fns

`component-provider` optionally takes in a context fn, which is applied each time the ctx map is passed to a component. It defaults to the `identity` function.

This can be incredibly useful for instrumentation, or for adding additional abstractions on top of the library (eg implementing your own data flow engine ala [domino](https://domino-clj.github.io/))

For example:

```clojure 
(defn ctx-transformer [ctx component]  
  (update ctx :log-ctx #(conj (or % []) (dom/component-name component))))

(dom/component-provider (system) ctx-transformer app)
```

# Testing

`rehook` promotes building applications with no singleton global state.
 Therefore, you can treat your components as 'pure functions', as all inputs to the component are passed in as arguments.

Testing (with React hooks) is a deeper topic that I will explore via a blog post in the coming months. Please check back!

 

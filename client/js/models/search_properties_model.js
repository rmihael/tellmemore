/**
 * Model that will contain a bunch of search
 * properties that allow components draw appropriate
 * elements on page
 * Author: bearz
 */
define([
    'underscore',
    'jquery',
    'backbone'
], function(_, $, Backbone) {
    var PropertiesModel = Backbone.Model.extend({

        initialize: function() {
            // dirty hack for now
            this.attributes = {
                "events": [
                    {"name": "Login", "type": "number"},
                    {"name": "Started New Game", "type": "number"},
                    {"name": "Finished Game", "type": "number"},
                    {"name": "Upgraded", "type": "number"}
                ],
                "user_facts": [
                    {"name": "Received newsletter", "type": "boolean"},
                    {"name": "Product type", "type": "enum", "values": ["trial", "business"]},
                    {"name": "Revenue", "type": "number"},
                    {"name": "Description", "type": "string"}
                ]
            }
        },
        /**
         * Returns list of user facts available for this user.
         */
        getUserFactsList: function() {
            return _.pluck(this.get("user_facts"), "name");
        },
        /**
         * Returns list of user events available for this user
         * @returns {Array}
         */
        getUserEventsList: function() {
            return _.pluck(this.get("events"), "name");
        },
        /**
         * Returns information needed to draw appropriate form
         * for user.
         */
        getUserFactDO: function(fact_name) {
            return _.filter(this.get('user_facts'), function(fact) { return fact['name'] == fact_name})[0];
        },
        /**
         * Returns information about this event needed
         * to draw appropriate selector for user
         * @param event_name
         * @returns {*}
         */
        getUserEventsDO: function(event_name) {
            return _.filter(this.get('events'), function(event) { return event['name'] === event_name })[0];
        }
    });

    return PropertiesModel;
});

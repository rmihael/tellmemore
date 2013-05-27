/**
 * Component that contains inside itself simple search controllers
 * that gathered one with each other using 'OR',
 * Component copy-paste a little the "and_item" behavior but it is likely to change,
 * so they are separate
 *
 * Author: bearz
 */
define([
    'jquery',
    'backbone',
    'underscore',
    'base_view',
    'text!components/search/templates/and_item_template',
    'components/search/js/search_select_component'
], function($, Backbone, _, BaseView, AndItemTemplate, SearchSelectComponent) {
    var OrSearchGroup = BaseView.extend({
        template: _.template(AndItemTemplate),
        className: "and-item row",
        initialize: function() {
            _.bindAll(this, "_removeSearchComponent");
            this.search_components = []; // will be used for gathering data later
        },
        events: {
            "click .remove-search-group": "_handleRemoveClick",
            "click .add-or": "_addSearchComponent"
        },

        render: function() {
            this.$el.html(this.template());
            this.components_container = $(this.el).find(".search-items");
            this._addSearchComponent();
            return this;
        },
        /**
         * Going to remove itself from the DOM.
         * @private
         */
        _handleRemoveClick: function() {
            this.options.remove_callback(this.cid);
            this.unbind();// unbind from any events attached to the view
        },
        /**
         * Creates and add to the DOM another search component.
         * @private
         */
        _addSearchComponent: function() {
            var search_component = new SearchSelectComponent({remove_callback: this._removeSearchComponent});
            this.components_container.append(search_component.el); // adding element to the container
            search_component.render();
            this.search_components.push(search_component);
        },
        /**
         * Removing component by id provided from internal collection.
         * If removal is allowed and ok, gives component explicit command to
         * remove itself from DOM.
         * @param component_id
         * @private
         */
        _removeSearchComponent: function(component_id) {
            // removing component from associated collection
            var component = null;
            for (var i=0; i < this.search_components.length; i++) {
                if (this.search_components[i].cid === component_id) {
                    component = this.search_components[i];
                    this.search_components.splice(i, 1);
                    break; // important because array changing in cycle
                }
            }
            component.el.remove();
            if (this.search_components.length === 0) {
                this._addSearchComponent(); // add one component if user removes the last one
            }
        }
    });

    return OrSearchGroup;
});

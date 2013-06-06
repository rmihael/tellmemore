/**
 * Base view of search. Renders itself into the element that
 * parent specified for him. Allow user to search/slice and dice his
 * user database finding needed info.
 * @version prototype 0.0.1
 * Author: bearz
 */
define([
    'backbone',
    'jquery',
    'underscore',
    'base_view',
    'text!templates/search_template',
    'components/search/js/search_group',
    'models/search_properties_model'
], function(Backbone, $, _, BaseView, SearchTemplate, SearchGroupComponent, PropertiesModel) {
    var SearchView = BaseView.extend({
        template: _.template(SearchTemplate),
        events: {
            "click .add-condition": "_addSearchComponent"
        },
        initialize: function() {
            _.bindAll(this, "_removeSearchComponent");
            this.search_components = []; // will be used for gathering data later
            this.properties_model = new PropertiesModel();
        },
        render: function() {
            this.$el.html(this.template());
            this.components_container = $(this.el).find(".search-components");
            this._addSearchComponent(); // add one component by default

            return this;
        },
        /**
         * Creates and add to the DOM another search component.
         * @private
         */
        _addSearchComponent: function() {
            var search_component = new SearchGroupComponent({remove_callback: this._removeSearchComponent,
                model: this.properties_model});
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
            component.remove();
            if (this.search_components.length === 0) {
                this._addSearchComponent(); // add one component if user removes the last one
            }
        }

    });

    return SearchView;
});

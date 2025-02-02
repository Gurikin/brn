import Route from '@ember/routing/route';
// eslint-disable-next-line ember/no-mixins
import AuthenticatedRouteMixin from 'ember-simple-auth/mixins/authenticated-route-mixin';
import { inject as service } from '@ember/service';
import NetworkService from 'brn/services/network';
import Intl from 'ember-intl/services/intl';
import type Store from '@ember-data/store';

export default class GroupsRoute extends Route.extend(AuthenticatedRouteMixin) {
  @service('network') network!: NetworkService;
  @service('store') store!: Store;
  @service('intl') intl!: Intl;
  queryParams = {
    locale: {
      type: 'string',
      refreshModel: true,
    },
  };
  async model() {
    await this.network.loadCurrentUser();
    return await this.store.query('group', {
      locale: this.intl.locale[0],
    });
  }
}

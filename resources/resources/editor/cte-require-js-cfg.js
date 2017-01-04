/*!
 * Copyright 2002 - 2017 Webdetails, a Pentaho company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

/**
 * RequireJS configuration file for CTE
 */

(function() {
  if(!requireCfg.map) requireCfg.map = {};
  if(!requireCfg.map['*']) requireCfg.map['*'] = {};
  if(!requireCfg.map['cte']) requireCfg.map['cte'] = {};

  //RequireJS css! loader plugin 0.1.8
  requireCfg.map['*']['css'] = 'cte/lib/require-css/css';

  requireCfg.config = requireCfg.config || {};

  var requirePaths = requireCfg.paths,
    requireShims = requireCfg.shim,
    requireConfig = requireCfg.config;

  if(!requireConfig['amd']) {
    requireConfig['amd'] = {};
  }
  if(!requireConfig['amd']['shim']) {
    requireConfig['amd']['shim'] = {};
  }
  var amdShim = requireConfig['amd']['shim'];

  var prefix = '';
  if(typeof CONTEXT_PATH !== 'undefined') { // production

    prefix = requirePaths['cte'] = CONTEXT_PATH + 'api/repos/cte/resources/editor';
  } else if(typeof FULL_QUALIFIED_URL !== 'undefined') { // embedded

    prefix = requirePaths['cte'] = FULL_QUALIFIED_URL + 'api/repos/cte/resources/editor';
  } else { // build
    prefix = requirePaths['cte'] = 'cte';
  }

  // RequireJS amd! loader plugin. Wraps non-AMD scripts as AMD modules on the fly,
  // to be used when a shim isn't enough (see plugin prescript and postscript).
  requirePaths['amd'] = prefix + '/lib/require-amd/nonamd';

  //jquery 1.12.4, without globally scoped variables
  requirePaths['cte/lib/jquery'] = prefix + '/lib/jQuery/jquery.min';
  requireShims['cte/lib/jquery'] = {
    exports: '$',
    init: function() {
      return $.noConflict(true);
    }
  };
  //mapping all jquery requests from inside cte to 'cte/lib/jquery'
  requireCfg.map['cte']['jquery'] = 'cte/lib/jquery';

  //bootstrap 3.3.7
  requirePaths['cte/lib/bootstrap'] = prefix + '/lib/Bootstrap/js/bootstrap.min';
  amdShim['cte/lib/bootstrap'] = {
    exports: 'jQuery',
    deps: {
      'cte/lib/jquery': 'jQuery'
      //'css!cte/lib/Bootstrap/css/bootstrap.min.css': '',
      //'css!cte/lib/Bootstrap/css/bootstrap-theme.min.css': ''
    }
  };

  //select2 3.5.0
  requirePaths['cte/select2'] = prefix + '/lib/select2';
  amdShim['cte/select2/select2.min'] = {
    exports: 'jQuery',
    deps: {
      'cte/lib/jquery': 'jQuery',
      'css!cte/select2/select2.min.css': ''
    }
  };

  //ACE 1.2.6
  requirePaths['ace'] = prefix + '/lib/ace/src';

  //ml-modal
  requirePaths['cte/ml-modal'] = prefix + '/ml-modal';
  amdShim['cte/ml-modal/js/ml-modal'] = {
    exports: 'Modal',
    deps: {
      'css!cte/ml-modal/css/ml-modal.css': ''
    }
  };



  //fileTree
  requirePaths['cte/fileTree'] = prefix + '/fileTree';
  amdShim['cte/fileTree/js/jqueryFileTree'] = {
    exports: 'jQuery',
    deps: {
      'cte/lib/jquery': 'jQuery',
      'css!cte/fileTree/css/jqueryFileTree.css': ''
    }
  };
})();

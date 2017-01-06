/*!
 * Copyright 2002 - 2017 Webdetails, a Pentaho company.  All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

define([
  "./aceWrapper",
  "amd!./ml-modal/js/ml-modal",
  "./lib/jquery",
  "./lib/bootstrap",
  "amd!./fileTree/js/jqueryFileTree",
  "amd!./select2/select2.min",
  "css!./cte.css"
], function(AceWrapper, Modal, $) {
  // the current editor mode
  var _editorMode = "";
  // stores URL parameters
  var _params = {};
  // stores currently opened file name
  var _fileName = "";
  // stores currently opened file provider
  var _fileProvider = "";
  // true if 'change' event was triggered, except when file is beeing loaded
  var _dirty = false;
  // if true _dirty will be set to true when 'change' events are triggered in the editor
  var _pageLoaded = false;

  var _editor = new AceWrapper();

  var _listeners = {
    onStatusUpdate: null,
    onSave: null,
    notify: function(msg, type) {
      console && console.log(type + " : " + msg); 
    }
  };

  function load(filename, provider) {

    _editor.loadFile(
      filename,
      provider,
      function(response, loadedFileName, loadedFileProvider) {

        _pageLoaded = false;

        // set filename in global var
        _fileName = loadedFileName;

        // set file provider in global var
        _fileProvider = loadedFileProvider;

        _editor.setContents(response);

        _pageLoaded = true;

        setDirty(false);

        // use menu selected mode or filename extension to set mode
        _editor.setMode((!_editorMode) ? loadedFileName.split('.').pop() : _editorMode);

        notify('File ' + loadedFileName + ' loaded.');

        updateStatus();

        if(window.history.pushState) {
          window.history.pushState("", "" , "" + buildUrl(loadedFileName, loadedFileProvider, true));
        }
      },
      function(response) {
        if(response) {
          if(typeof response.error == "function") {
            notify("Error loading file: " + response.error().statusText);
          } else {
            notify(response);
          }
        }
      }
    );
  }

  function save() {

    _editor.saveFile(
      _fileName,
      _fileProvider,
      function(response) {
        var msg = "";
        if(response && response.toString() === "true") {
          _pageLoaded = true;
          setDirty(false);
          notify(_fileName + ' saved.');
        } else {
          notify('Error saving file: ' + _fileName, 'error');
        }
      },
      function(response) {
        notify(response, 'error');
      }
    );
  }

  var _notifyType = {
    ERROR: 'error',
    INFO: 'info',
    WARN: 'warn'
  };
  function notify(msg, type) {
    if(type == null) {
      type = _notifyType.INFO;
    }
    if(typeof(_listeners.notify) === 'function') {
      _listeners.notify(msg, type);
    }
  }

  function updateStatus() {
    //var infoWarning = (_editor.isReadOnly() && pageLoaded ? ' (readonly)' : '');
    var infoWarning,
      $saveBtn = $("#saveBtn"),
      $infoArea = $('#infoArea');

    if(_fileName && _fileName.length > 0) {
      //$infoArea.removeClass("disabled");
      $infoArea.show();
    } else {
      //$infoArea.addClass("disabled");
      $infoArea.hide();
    }

    if(_editor.isReadOnly()) {
      $saveBtn.addClass("disabled");
      $saveBtn.removeClass("btn-success");

      $infoArea.removeClass("alert-warning");
      $infoArea.removeClass("alert-success");
      $infoArea.addClass("alert-danger");

      if(_fileName && _fileName.length > 0) {
        $('#infoAreaWarnings').text('(readonly)');
      }
    } else {
      // manage enable/disable save button

      if(_dirty && !_editor.isReadOnly()) {
        //$saveBtn.prop("disabled", false).css("cursor", "pointer").fadeTo(500, 1);
        $saveBtn.removeClass("disabled").fadeTo(500, 1);
        $saveBtn.addClass("btn-success");

        $infoArea.removeClass("alert-danger");
        $infoArea.removeClass("alert-success");
        $infoArea.addClass("alert-warning");

        $('#infoAreaWarnings').text('*');
      } else {
        //$saveBtn.prop("disabled", true).css("cursor", "default").fadeTo(500, 0.2);
        $saveBtn.addClass("disabled");
        $saveBtn.removeClass("btn-success");

        $infoArea.removeClass("alert-danger");
        $infoArea.removeClass("alert-warning");
        $infoArea.addClass("alert-success");

        $('#infoAreaWarnings').text('');
      }
    }

    //$infoArea.text(_fileName);
    if(_fileName.length > 50) {

      $infoArea.html("<span>(...)/</span>"
        + "<span class='bold'>" + _fileName.substr(_fileName.lastIndexOf("/") + 1) + "</span>");
    } else {
      var _fileNameIndex = _fileName.lastIndexOf("/") + 1;

      $infoArea.html(
        // path
        "<span>" + _fileName.substr(0, _fileNameIndex) + "</span>"
        // file name
        + "<span class='bold'>" + _fileName.substr(_fileNameIndex) + "</span>");
    }
    $infoArea.prop("title", _fileName);

    if(typeof(_listeners.onStatusUpdate) === "function") {
      _listeners.onStatusUpdate(_dirty);
    }
  }

  function buildUrl(filePath, fileProvider, encode) {
    var url = window.location.origin + window.location.pathname;

    if(filePath) {
      url += "?path=" + (encode ? encodeURIComponent(filePath) : filePath);
    
      // no sense in having a provider param if we don't have a path param
      if(fileProvider) {
        url += "&provider=" + (encode ? encodeURIComponent(fileProvider) : fileProvider); 
      }
    } 

    return url;
  }

  function setDirty(dirty) {
    var wasDirty = _dirty;
    _dirty = dirty;

    if(_dirty != wasDirty) {
      if(!wasDirty && _dirty) {
        setExitNotification(true);
      } else if(wasDirty && !_dirty) {
        setExitNotification(false);
      }
      updateStatus();
    }
  }

  function setExitNotification(enable) {
    if(window.parent && window.parent != window) {//only mess with unload if owning the window
      return;
    }

    if(enable) {
      window.onbeforeunload = function(e) {
        return 'Any unsaved changes will be lost.';
      }
    } else {
      window.onbeforeunload = function() {null};
    }
  }


  /*
   * events
   */

  $(document).ready(function() {
    // read url query parameters
    var queryParam,
      queryParams = window.location.search.substring(1),
      myRegex = /([^&=]+)=?([^&]*)/g;

    while((queryParam = myRegex.exec(queryParams)) !== null) {
      _params[decodeURIComponent(queryParam[1])] = decodeURIComponent(queryParam[2]);
    }


    if(_params.editorOnly) {
      $('#btnArea').css('display', 'none');
      $('#editArea').css('margin-top', 0);
      $('#editArea').css('margin-bottom', 0);
    }

    var height = window.innerHeight - (_params.editorOnly ? 20 : 100);

    $('#fileTreeAccordion').height(height);
    $('.collapser-col').height(height);
    $('#editArea').height(height);

    //$('#fileTreeAccordion').width(window.innerWidth - (window.innerWidth * 0.75) - 25);
    //$('#editArea').width('75%');

    _editor.initEditor('editArea', save);

    if(_params.theme) {
      _editor.setTheme(_params.theme);
    }

    if(_params.mode) {
      _editor.setMode(_params.mode);
    }

    _editor.onChange(function(e) {
      //console.log(e.data.action);
      if(_pageLoaded) {
        setDirty(true);
      }
    });

    // defaults settings
    var $saveBtn = $("#saveBtn");
    //$("#saveBtn").prop("disabled", true).css("cursor", "default").fadeTo(500, 0.2);
    $saveBtn.addClass("disabled").fadeTo(500, 0.2);
    $saveBtn.removeClass("btn-success");
    _editor.setReadOnly(true);

    if(_params.path) {
      // More info, see aceWrapper.js
      load(_params.path, _params.provider);
    } else {
      _fileName = "";//"Unsaved";
      updateStatus();
    }


    /********************
     **** FileTree ******
     ********************/
      // custom confirm/discard dialog
    var confirmDiscard = new Modal({
        header: true,
        content: "You have unsaved changes. </br> Do you wish to save them or proceed without saving?",
        confirmDialog: true,
        saveBtn: true,
        dontSaveBtn: true,
        cancelBtn: true
      });

    var loadAnother = function(f , p) {
      $(".selectedFile").attr("class", "");
      $("a[rel='" + f + "']").attr("class", "selectedFile");
      load("/" + f , p);
    };

    _editor.getProviders(function f(providers) {
      if(providers && providers.length > 0) {
        for(i = 0; i < providers.length; i++) {
          var providerId = providers[i].id;
          var providerName = providers[i].name;

          var $divTreePanel = $('<div class="panel panel-default">')
          .append('<div class="panel-heading" provider-id="' + providerId + '"><h4 class="panel-title"><a data-toggle="collapse" data-parent="#fileTreeAccordion" href="#panel-' + providerId + '" >' + providerName + '</a></h4><h4 class="refresh-provider" provider-id="' + providerId + '" provider-name="' + providerName + '" /></div>');
          var $divTreeCollapsible = $('<div id="panel-' + providerId + '" class="panel-collapse collapse">');
          var $divTree = $('<div id="treeDiv-' + providerId + '"  provider-id="' + providerId + '"provider-name="' + providerName + '" class="urltargetfolderexplorer">');

          $divTree.fileTree({
              root: "/",
              providerId: providerId,
              providerName: providerName,
              script: "/pentaho/plugin/cte/api/tree?showHiddenFiles=true&provider=" + providerId,
              expandSpeed: 500,
              collapseSpeed: 500,
              multiFolder: false,
              folderClick: function(obj, folder) {
                if($(".selectedFolder").length > 0) {
                  $(".selectedFolder").attr("class", "");
                }
                $(obj).attr("class", "selectedFolder");
              }
            },
            function(file, provider) {

              if(_dirty) {

                confirmDiscard.open();

                // 'save' button already has an attached event listener that handles the file saving
                $('div.ml-confirm-dialog > button.save').click(function (data, fn) { loadAnother(file, provider); });

                $('div.ml-confirm-dialog > button.no-save').click(function (data, fn) { loadAnother(file, provider); });

              } else {
                loadAnother(file, provider);
              }

            });

          $divTreeCollapsible.append($('<div class="panel-body">').append($divTree));
          $divTreePanel.append($divTreeCollapsible);
          $('#fileTreeAccordion').append($divTreePanel);

          $('.panel-title a').addClass('collapsed');
        }
      }
    });

    $('#fileTreeAccordion h4.refresh-provider').on('click', function() {
      var providerId = $(this).attr('provider-id');
      var providerName = $(this).attr('provider-name');
      if(providerId) {
        $('div#treeDiv-' + providerId).fileTree({
            root: "/",
            providerId: providerId,
            providerName: providerName,
            script: "/pentaho/plugin/cte/api/tree?showHiddenFiles=true&provider=" + providerId,
            expandSpeed: 500,
            collapseSpeed: 500,
            multiFolder: false,
            folderClick: function(obj, folder) {
              if($(".selectedFolder").length > 0) {
                $(".selectedFolder").attr("class", "");
              }
              $(obj).attr("class", "selectedFolder");
            }
          },
          function(file, provider) {

            if(_dirty) {

              confirmDiscard.open();

              // 'save' button already has an attached event listener that handles the file saving
              $('div.ml-confirm-dialog > button.save').click(function (data, fn) { loadAnother(file, provider); });

              $('div.ml-confirm-dialog > button.no-save').click(function (data, fn) { loadAnother(file, provider); });

            } else {

              loadAnother(file, provider);
            }
          });
      }
    });


    $('#fileTreeAccordion h4.panel-title > a').on('click', function() {

      // first, hide all refresh icons
      $('#fileTreeAccordion h4.refresh-provider').each(function() { $(this).css('display', 'none'); });

      // determine if this is an action of a provider that is now being expanded
      var providerWasCollapsed = $(this).attr('class') == 'collapsed';
      var refreshIconDisplayVal = providerWasCollapsed ? 'block' : 'none'; // new refresh icon display value

      // update this provider's refresh icon display value accordingly
      $(this).closest('div.panel-heading').find('h4.refresh-provider').css('display', refreshIconDisplayVal);
    });

    $('#collapser-img').on('click', function() {

      // determine the current status of the providerListColumn ( collapsed / expanded )
      var providersListColumnIsCollapsed = $('div.content-row div.left-col.collapsible').css('display') == 'none';

      var providersListColumnNewVal = providersListColumnIsCollapsed ? 'table-cell' /* default */ : 'none';
      var collapserOldArrow = providersListColumnIsCollapsed ? 'arrow-right.png' /* default */ : 'arrow-left.png';
      var collapserNewArrow = providersListColumnIsCollapsed ? 'arrow-left.png' /* default */ : 'arrow-right.png';

      $('div.left-col.collapsible').css('display', providersListColumnNewVal);

      var imgSrc = $('img#collapser-img').attr('src');
      $('img#collapser-img').attr('src' , imgSrc.replace(collapserOldArrow, collapserNewArrow));

    });

    /**********************************
     **** editor mode select box ******
     **********************************/
    $("#modes")
    .change(function() {
      _editorMode = $(this).val();
      // auto detect
      if(!_editorMode) {
        _editorMode = (!_fileName) ? "" : _fileName.split('.').pop();
      }
      _editor.setMode(_editorMode);
      notify("Requested editor mode: " + _editorMode, 'info');
    });

  });

  $(window).resize(function() {
    var height = window.innerHeight - (_params.editorOnly ? 20 : 100);
    //$('#fileTreeAccordion').height(height);
    $('.collapser-col').height(height);
    $('#editArea').height(height);
  });

  // force WebKit to redraw/repaint to propagate style changes
  $(document).click(function() {
    var el = document.getElementById('fileTreeAccordion');
    el.style.display = 'none';
    el.offsetHeight; // no need to store this anywhere, the reference is enough
    el.style.display = '';
  });

  // enable dragging the collapser div
  var dragging = false;
  $('div.collapser-col').mousedown(function(e) {
    e.preventDefault();

    dragging = true;
    var main = $('div.right-col');

    $(".collapser-col").css("background-color", "#DCDCDC");

    $(document).mousemove(function(e) {
      $('div.left-col.collapsible').css("width", ((e.pageX / window.innerWidth) * 100) + "%").css("width", "-=13px");
    });
  });

  $(document).mouseup(function(e) {
    if(dragging) {
      $('div.left-col.collapsible').css("width", ((e.pageX / window.innerWidth) * 100) + "%").css("width", "-=13px");

      $(document).unbind('mousemove');
      dragging = false;

      $(".collapser-col").css("background-color", "");
    }
  });


  $(function() {
    $(".modes").select2({width: '120px'});
  });


  $(function() {
    $('.modes').select2({width: '140px'});

    /*svg support check*/
    if(document.implementation.hasFeature('http://www.w3.org/TR/SVG11/feature#Image', '1.1')) {
      $('html').addClass('svg');
    } else $('html').addClass('no-svg');

    var shortcutsModal = new Modal({
      content: "<div class='popup-keys-table'>\n<table class='table'>\n<thead>\n<tr>\n<th>Action</th>\n<th>PC ( Win/Linux )</th>\n<th>Mac</th>\n</tr>\n</thead>\n<tbody>\n<tr>\n<td>Save</td>\n<td><code>Ctrl + S</code></td>\n<td> <code>Command + S</code></td>\n</tr>\n<tr>\n<td>Find</td>\n<td><code>Ctrl + F</code></td>\n<td><code>Command + F</code></td>\n</tr>\n<tr>\n<td>Find Next</td>\n<td><code>Ctrl + K</code></td>\n<td><code>Command + G</code></td>\n</tr>\n<tr>\n<td>Find Previous</td>\n<td><code>Ctrl + Shift + K</code></td>\n<td><code>Command + Shift + G</code></td>\n</tr>\n<tr>\n</tbody>\n</table>\n</div>\n<div>\n<span class='popup-keys-label'>For a complete list of shortcuts please refer to this <a href='https://github.com/ajaxorg/ace/wiki/Default-Keyboard-Shortcuts' target='_blank'>link</a> </span>\n</div>",
      maxWidth: 500,
      header: true
    });

    $(".info-btn").click(function() {
      shortcutsModal.open({closeButton: true});
    });
  });

  return {
    load: load,
    save: save,
    notify: notify,
    updateStatus: updateStatus,
    setDirty: setDirty,
    setExitNotification: setExitNotification
  };
});
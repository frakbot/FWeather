/*
 * Copyright 2014 Sebastiano Poggi and Francesco Pontillo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

module.exports = function (grunt) {
  // load all grunt tasks
  require('load-grunt-tasks')(grunt);

  // Read the licenses and the related texts
  var licenses = grunt.file.readJSON('licenses.json');
  console.info('Read licenses:');
  console.info(licenses);
  for (var l in licenses) {
    var licenseText = grunt.file.read('files/' + licenses[l].short + '.txt');
    licenses[l].text = licenseText;
  }

  grunt.initConfig({
  	config: {
	  	license: '../assets/www'
  	},
  	clean: {
      options: { force: true },
      dist: ['<%= config.license %>/license.html']
    },
    processhtml: {
	    options: {
	      data: {
          licenses: licenses
	      }
	    },
	    dist: {
	      files: {
	        '<%= config.license %>/license.html': ['license.html']
	      }
	    }
	  }
  });

  grunt.registerTask('default', ['clean', 'processhtml']);
};
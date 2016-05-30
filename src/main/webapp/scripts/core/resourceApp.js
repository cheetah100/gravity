var app = angular.module('resourceApp', []);

app.controller('boardCtrl', function($scope, $http) {
	
  $http.get("/gravity/spring/board")
  .success(function (response) { $scope.boards = response; });
  
  $scope.setBoard = function(key,name) {
            $scope.board = name;
            $scope.boardid = key;
            fullUrl = "/gravity/spring/board/" + key;
            $http.get(fullUrl).success(function (response) {
            	$scope.boardData = response;            	                 		
           		$scope.getResources();
            });
  }
    
  $scope.getResources = function() {
      url = "/gravity/spring/board/" + $scope.boardid + "/resources";
      $http.get(url).success(function (response) {
    	  $scope.resources = response;
    	  $scope.blah = 'blah';
      });
  }
});


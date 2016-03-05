var app = angular.module('gravityApp', []);

app.controller('boardCtrl', function($scope, $http) {
	
  $http.get("/gravity/spring/board")
  .success(function (response) { $scope.boards = response; });
  
  $scope.setBoard = function(key,name) {
            $scope.board = name;
            $scope.boardid = key;
            fullUrl = "/gravity/spring/board/" + key;
            $http.get(fullUrl).success(function (response) {
            	$scope.boardData = response;            	
            
            	var phases=[];
            	for( key in response.phases ) {
            		phases.push(response.phases[key]);
           		}
           		$scope.phases = phases;
           		$scope.phase = phases[0].name;
           		$scope.phaseid = phases[0].id;
           		
           		$scope.views = response.views;
           		$scope.filters = response.filters;
           		
           		$scope.getCards();
 
            });
  }
  
  $scope.setPhase = function(key,phase) {
  	$scope.phase = phase.name;
    $scope.phaseid = key;
    $scope.getCards();
  }
  
  $scope.setFilter = function(key,filter) {
  	if(key==''){
		$scope.filter = 'Filter';
    	$scope.filterid = key;
    } else {  				
    	$scope.filter = filter.name;
    	$scope.filterid = key;
    }            
  }
  
  $scope.setView = function(key,view) {
  	if(key==''){
		$scope.view = 'View';
    	$scope.viewid = key;
    } else {  				
    	$scope.view = view.name;
    	$scope.viewid = key;
    }            
  }
  
  $scope.getCards = function() {
      url = "/gravity/spring/board/" + $scope.boardid + "/phases/" + $scope.phaseid + "/cards";
      $http.get(url).success(function (response) {
    	  $scope.cards = response;
      });
  }
  
  
});


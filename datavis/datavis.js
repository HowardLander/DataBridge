var defWidth = 960, defHeight = 600,  
	padding = 1.5, // separation between same-color nodes
	clusterPadding = 6, // separation between different-color nodes
	minRadius = 4, maxRadius = 10;
var width, height, curFileName="";
var color = d3.scale.category20();
var node_stroke_clr = d3.rgb(142, 186, 229).darker();
var node_multicluster_stroke_clr = d3.rgb(10, 10, 10);
var linkedByIndex = {};
var clusterCenterNodes = {};
var e = document.documentElement;
var g = document.getElementsByTagName('body')[0];
var bDragOn = false;
var lastSelNode = null, lastSelLink = null;
var lastSelNodeId = -1, lastSelEdgeSource = -1, lastSelEdgeTarget = -1, selNodeId = -1, selEdgeSource=-1, selEdgeTarget=-1;
var overlayRect, svg, zoom, force, link, node;
var simVal = 0.6, oriData="", filterData, filterNodeData, filterLineData, RNodeVal="firstnode", existNodes = [], clusterLevel = 0, subClusterOn = true, tooltip;
var lastSearchStr = "", lastSearchNodes = new Array();
var bDynamicLayout = true;
	
// called in index.php <script>
function init() {	
	width = window.innerWidth || e.clientWidth || g.clientWidth;
	height = window.innerHeight || e.clientHeight || g.clientHeight;
	if(width <= 0) 
		width = defWidth;	
	else
		width -= 100;	
	if(height <= 0) 
		height = defHeight; 
	else
		height -= 300; // account for height of upper and lower gui elements
	// set width for #datainfo div
	document.getElementById("datainfo").style.width=width+"px";		
	
	// create tooltip div in body element
	tooltip = d3.select("body").append("div")   
		.attr("class", "tooltip")               
		.style("visibility", "hidden");
	// change the default selected value for similarity filter value
	document.getElementById('sim-value').value = simVal;    
	document.getElementById("togglelayout").value = "Pause layout";
	zoom = d3.behavior.zoom(); 	
	// create svg for #chart div
	svg = d3.select("#chart").append("svg")
		.attr("width", width)
		.attr("height", height)
		.append("g")
		.call(zoom.scaleExtent([1, 4]).on("zoom", zoom_redraw))
		.append("g");
	// create rect in svg.g.g
	overlayRect = svg.append("rect")
		.attr("class", "overlay")
		.attr("width", width)
		.attr("height", height);

	force = d3.layout.force()
		//.linkStrength(0.1)
		.friction(0.9)
		//.linkDistance(20)
		//.charge(-30)
		//.gravity(0.1)
		.theta(0.8)
		.alpha(0.1)
		.gravity(.06)
		.charge(-120)
		.linkDistance(60)
		.size([width, height]);
	window.onresize = updateWindow;
	// File shown on the screen
	FileChange();
	contextMenuShowing = false;
	d3.select("body").on('contextmenu', function(d, i) {
		if (contextMenuShowing) {
			d3.event.preventDefault();
			d3.select(".popup").remove();
			contextMenuShowing = false;
		} else {
			d3_target = d3.select(d3.event.target);
			if (d3_target.classed("circle")) {
				d3.event.preventDefault();
				contextMenuShowing = true;
				d = d3_target.datum();
				canvas = d3.select(".canvas");
				mousePosition = d3.mouse(canvas.node());
				popup = canvas.append("div")
					.attr("class", "popup")
					.style("left", mousePosition[0] + "px")
					.style("top", mousePosition[1] + "px");
					
				//popup.append("h2").text(d.title);
				popup.append("p")
					.append("a")
					.attr("href",d.URL)
					.attr("target", "_blank")
					.text(d.title);
				popup.append("p")
					.append("a")
					.attr("href",d.URL)
					.attr("target", "_blank")
					.text("Search by similarity");
				// button for go back to the former level of clustering
				popup.append("p")
					.append("button")
					.on("click", function(dd) {
						GoBackCluster();
						d3.select(".popup").remove();
						contextMenuShowing = false;
					})
					.text("Go back to the former level of clustering.");
				canvasSize = [
					canvas.node().offsetWidth,
					canvas.node().offsetHeight
				];
				
				popupSize = [ 
					popup.node().offsetWidth,
					popup.node().offsetHeight
				];
				
				if (popupSize[0] + mousePosition[0] > canvasSize[0]) {
					popup.style("left","auto");
					popup.style("right",0);
				}
				
				if (popupSize[1] + mousePosition[1] > canvasSize[1]) {
					popup.style("top","auto");
					popup.style("bottom",0);
				}	
			}
		}
	});
}

function ResetView(){
	document.getElementById("sim-value").value = 0.6;
	simVal = 0.6;
	document.getElementById("data-chooser").value = "DbFNNetwork-7.3-louvain";
	document.getElementById("RNode-chooser").value = "firstnode";
	document.getElementById("datainfo").html("");
	FileChange();
}

// called in init()
// set to window.onresize
function updateWindow(){
    width = window.innerWidth || e.clientWidth || g.clientWidth;
    height = window.innerHeight|| e.clientHeight|| g.clientHeight;
    if(width <= 0) 
		width = defWidth;
	else
		width -= 100;		
	if(height <= 0) 
	 	height = defHeight; 
	else
		height -= 300; // account for height of upper and lower gui elements
    overlayRect.attr("width", width).attr("height", height);
    svg.attr("width", width).attr("height", height);
    document.getElementById("datainfo").style.width=width+"px";	
    force.size([width, height]);
}
		
// called in init()
function zoom_redraw() {
	 svg.attr("transform", "translate(" + d3.event.translate + ")scale(" + d3.event.scale + ")");
    //svg.attr("transform", "scale(" + d3.event.scale + ")");
} 

// used in SetupData() and SimValueChange()
var node_drag = d3.behavior.drag()
	.on("dragstart", dragstart)
	.on("drag", dragmove)
	.on("dragend", dragend);	
	
// called in var node_drag
function dragstart(d, i) {
	d3.event.sourceEvent.stopPropagation(); // very important; otherwise, panning will interfare with node dragging
	force.stop(); // stops the force auto positioning before you start dragging
	bDragOn = true;
}

// called in var node_drag
function dragmove(d, i) {
	d.px += d3.event.dx;
	d.py += d3.event.dy;
	d.x += d3.event.dx;
	d.y += d3.event.dy; 
	tick(); // this is the key to make it work together with updating both px,py,x,y on d !
}

// called in var node_drag
function dragend(d, i) {
	d.fixed = true; // of course set the node to fixed so the force doesn't include the node in its auto positioning stuff
	tick();
	bDragOn = false;
	if(bDynamicLayout)
		force.resume();
	
}

// called in fadeRelativeToNode()
function isConnected(a, b) {
	return linkedByIndex[a.index + "," + b.index] || linkedByIndex[b.index + "," + a.index] || a.index == b.index;
}

// called in SetupData(), dragmove(), and dragend()
function tick(e) {
	node
		.attr("transform", function(d) { 
			d.x = Math.max(maxRadius, Math.min(width - maxRadius, d.x));
			d.y = Math.max(maxRadius, Math.min(height - maxRadius, d.y));
			return "translate(" + d.x + "," + d.y + ")"; 
		});
	
	if(!bDragOn) {
		node
			.each(cluster(10 * e.alpha * e.alpha))
			.each(collide(.5))
			.attr("cx", function(d) { return d.x; })
			.attr("cy", function(d) { return d.y; });
	}
	
	// add the curvy lines
	link.attr("d", function(d) {
		var dx = d.target.x-d.source.x, dy = d.target.y-d.source.y, dr = Math.sqrt(dx * dx + dy * dy);
		return "M" + d.source.x + "," + d.source.y + "A" + dr + "," + dr + " 0 0,1 " + d.target.x + "," + d.target.y;
	});
};

// Move d to be adjacent to the cluster node.
// called in tick() and collide()
function cluster(alpha) {	
	return function(d) {
		var node = clusterCenterNodes[d.clr], l, r, x, y;
		if (node == d) return;
		x = d.x - node.x;
		y = d.y - node.y;
		l = Math.sqrt(x * x + y * y);
		//r = d.radius + node.radius;
		r = maxRadius + maxRadius;
		if (l != r) {
			l = (l - r) / l * alpha;
			d.x -= x *= l;
			d.y -= y *= l;
			node.x += x;
			node.y += y;
		}
	}
}

// Resolves collisions between d and all other circles.
// called in tick()
function collide(alpha) {
    if(typeof filterNodeData === "undefined")
        return true; 
    var quadtree = d3.geom.quadtree(filterNodeData);
    if(typeof quadtree === "undefined")
        return true;
	return function(d) {
		//var r = d.radius + radius.domain()[1] + padding,
		var r = maxRadius + maxRadius,
			nx1 = d.x - r,
			nx2 = d.x + r,
			ny1 = d.y - r,
			ny2 = d.y + r;
		quadtree.visit(function(quad, x1, y1, x2, y2) {
			if (quad.point && (quad.point !== d)) {
				var x = d.x - quad.point.x,
					y = d.y - quad.point.y,
					l = Math.sqrt(x * x + y * y),
					//r = d.radius + quad.point.radius + (d.color !== quad.point.color) * padding;
					r = maxRadius + maxRadius + (d.cluster === quad.point.cluster ? padding : clusterPadding);
				if (l < r) {
					l = (l - r) / l * alpha;
					d.x -= x *= l;
					d.y -= y *= l;
					quad.point.x += x;
					quad.point.y += y;
				}
			}
			return x1 > nx2 || x2 < nx1 || y1 > ny2 || y2 < ny1;
		});
	};
}

// called in fadeRelativeToLink() and SetupLink()
function linkOpacity(val) {
	if(val<0.3)
		return 0.2;
	else if(val<0.5)
		return 0.3;
	else if (val < 0.7)
		return 0.5;
	else if (val < 0.8)
		return 0.7;
	else
		return 0.8;
}

// called in SetupData()
function fadeRelativeToNode(opacity) {
	return function(d) {		
		if(opacity < 1) {
			node.style("opacity", function(o) {
				var thisOpacity = isConnected(d, o) ? 1 : opacity;
				this.setAttribute('stroke-opacity', thisOpacity);
				if(thisOpacity == 1 && d!=o) {
					d3.select(this).select("text").transition()
						.duration(200)
						.style("visibility", "visible");
				}
				else {
					d3.select(this).select("text").transition()
						.duration(200)
						.style("visibility", "hidden");
				}
				return thisOpacity;
			});

			//link.style("stroke-opacity", function(o) {
			//	return o.source === d || o.target === d ? 1 : opacity;
			//});
			var htmltext;
			if(d.multicluster <= 1) // only belong one cluster
				htmltext = d.title;
			else { // belong to multiple clusters
				var grpary = String(d.group).split(","), index;
				htmltext = d.title + ", in groups ";
				for	(index = 0; index < grpary.length; index++) {
					var hexclr =  d3.rgb(color(+grpary[index]));
					htmltext += "<span class=\"numberCircle\" style=\"background:"+hexclr+"\">" + grpary[index].trim() + "</span>";					
				}
			}
			tooltip.html(htmltext)  
				.style("left", (d3.event.pageX) + "px")     
				.style("top", (d3.event.pageY - 28) + "px");

			tooltip.transition()        
				.duration(200)      
				.style("visibility", "visible");    
		}
		else { // return to no-fading
			node.style("opacity", function(o) {
				this.setAttribute('stroke-opacity', 0.9);
				d3.select(this).select("text").transition()
						.duration(200)
						.style("visibility", o.multicluster>1?"visible":"hidden");
			});
			
			//link.style("stroke-opacity", 0.6);
			tooltip.transition()        
				.duration(500)      
				.style("visibility", "hidden");
		}
	}
}  

// called in SetupLink()
function fadeRelativeToLink(opacity) {
	return function(d) {
		if (typeof(node) != "undefined")
			node.style("stroke-opacity", function(o) {
				var thisOpacity = (o==d.source || o==d.target ? 0.9 : opacity);
				this.setAttribute('opacity', thisOpacity);
				return thisOpacity;
			});

		link.style("stroke-opacity", function(o) {
			return o === d ? 1 : opacity;
		});
		if(opacity < 1 && d3.event != null) {
			tooltip.transition()        
				.duration(200)      
				.style("visibility", "visible");      
			tooltip.html("Similarity measure: "+d.value)
				.style("left", (d3.event.pageX) + "px")     
				.style("top", (d3.event.pageY - 28) + "px");
		}
		else { // return to no-fading
			link.style("stroke-opacity", function(o) {
				return linkOpacity(o.value);				
			});
			tooltip.transition()        
				.duration(500)      
				.style("visibility", "hidden");
		}
	}
}	

// called in FileChange() and SimValueChange()
// for #sim-value filter
function FilterBySimVal(aryvalue, index, ar) {
	
	if (aryvalue.value <= simVal){
		return false;
	} else {
		return true;
	}
	
	
}	

// called in FileChange()
// for Group Representative Point filter
function FilterByGRepresent(aryvalue, index, ar) {
	
	if (RNodeVal == "firstnode") {
		// gfirst
		if (aryvalue.gfirst[clusterLevel] == 0) {
			return false;
		} else {
			existNodes.push(aryvalue.index_o); 
			return true;
		}
	} else if (RNodeVal == "valuemax") {
		// valuemax
		if (aryvalue.valuemax[clusterLevel] == 0) {
			return false;
		} else {
			existNodes.push(aryvalue.index_o); 
			return true;
		}
	} else if (RNodeVal == "connectionmax") {
		// connectionmax
		if (aryvalue.connectionmax[clusterLevel] == 0) {
			return false;
		} else {
			existNodes.push(aryvalue.index_o); 
			return true;
		}
	}
	
}	

// called in SetupData() and SimValueChange()
// create links
function SetupLink() {
	// append path for each link
	// show in the DOM
	// does not show on the page?
	// filterData only have filtered link, no nodes data
	link = svg.selectAll("path.link")
		.data(filterData);
	
	link.enter().append("path")
		.attr("class", "link")
		.style("stroke-opacity", function(d) { return linkOpacity(d.value); })
		.style("stroke-width", 2)
		.on("mouseover", fadeRelativeToLink(0.3))
		.on("mouseout", fadeRelativeToLink(1))
		.on("click", function(d) {
			// clear out previously clicked/hgted other links if any
			if(lastSelLink != null)
				lastSelLink.style("stroke", "#999");
			selEdgeSource = d.source;
			selEdgeTarget = d.target;	
			if(lastSelEdgeSource != selEdgeSource || lastSelEdgeTarget != selEdgeTarget) {	
				lastSelLink = d3.select(this);
				lastSelLink.transition() 
					.duration(500)
					.style("stroke", "red");
				d3.select("#datainfo").html("Similarity measure between node " + d.source.title + " and node " + d.target.title + " is "+d.value);
				lastSelEdgeSource = selEdgeSource;
				lastSelEdgeTarget = selEdgeTarget; 	
			}
			else {// clear out the selection if the selected link is clicked again
				d3.select(this).transition() 
					.duration(500)
					.style("stroke", "#999");
				lastSelLink = null;	
				lastSelEdgeSource = -1;
				lastSelEdgeTarget = -1;
				d3.select("#datainfo").html(""); 	
			}
			// clear out previously clicked/hgted other nodes if any
			if(lastSelNode != null) {
				lastSelNode.style("stroke", node_stroke_clr);
				lastSelNode = null;
				lastSelNodeId = -1;
			}
		});
	link.exit().remove(); // very important, otherwise, old data will not be removed
	link.style("stroke-opacity", function(d) {
			return linkOpacity(d.value);
		});
	return link;
}

// called in FileChange()
// create graph
function SetupData() {
	// remove g elements inside svg
	svg.selectAll('g').remove();
	// create link
	link = SetupLink();
	
	// append g for each node
	// show in the DOM
	// does not show on the page?
	// nodes data are in filterNodeData
	node = svg.selectAll(".node")
		.data(filterNodeData);
	node.exit().remove();	
	
	var gnode = node.enter().append("g")
		.attr("id", function(d, i) {var result = "N" + i; return result;})
		.attr("data-legend",function(d) { return d.clr})
		.attr("data-legend-color",function(d) { return color(d.clr)})
		.on("click", function(d) {
			if(lastSelNodeId != d.index) {
				// clear out previously clicked/hgted other nodes if any
				if(lastSelNode != null) {
					lastSelNode.style("stroke", function(d) { 
									if(d.multicluster<=1)
										return node_stroke_clr;
									else
										return node_multicluster_stroke_clr;
									})
								.style("stroke-width", function(d) { 
									return d.multicluster; 
									});					
				}
				lastSelNode = d3.select(this).select("circle");
				lastSelNode.transition() 
					.duration(500)
					.style("stroke", d3.rgb(255, 0, 0));
				var htmltext;
				if(d.multicluster <= 1) // only belong to one cluster
					htmltext = "<h2>"+d.title+"</h2>"+d.description;
				else { // belong to multiple clusters
					var grpary = String(d.group).split(","), index;				
					htmltext = "<h2>"+d.title + ", in groups ";
					for	(index = 0; index < grpary.length; index++) {
						var hexclr =  d3.rgb(color(+grpary[index]));
						htmltext += "<span class=\"numberCircle\" style=\"background:"+hexclr+"\">" + grpary[index].trim() + "</span>";					
					}
					htmltext += "</h2>"+d.description;
				}
				d3.select("#datainfo").html(htmltext);
				lastSelNodeId = d.index; 	
			}
			else {// clear out the selection if the selected node is clicked again
				lastSelNode = null;
				lastSelNodeId = -1;
				d3.select(this).select("circle").transition() 
					.duration(500)
					.style("stroke", function(d) { 
						if(d.multicluster<=1)
							return node_stroke_clr;
						else
							return node_multicluster_stroke_clr;
						})
					.style("stroke-width", function(d) { 
						return d.multicluster; 
						});	
				d3.select("#datainfo").html(""); 	
			}
			// clear out previously clicked/hgted link if any
			if(lastSelLink != null) {
				lastSelLink.style("stroke", "#999");
				lastSelLink = null;
				lastSelEdgeSource = -1;
				lastSelEdgeTarget = -1;
			}
			ShowNodesInCluster(d.clr);
		})	
		.call(force.drag)
			.on("mouseover", fadeRelativeToNode(0.3))
			.on("mouseout", fadeRelativeToNode(1))
		.call(node_drag);	
	
	bDynamicLayout = true;
	document.getElementById("togglelayout").value = "Pause layout";
	
	// force?
	force
		.nodes(node.data())
		.links(link.data())
		//.linkStrength(0) // remove link influence to force-layout so that layout is only controlled by node clusters
		//.linkStrength(function(d, i) {return (d.value - 0.5>0 ? 0.01 : 0)})
		.linkStrength(function(d, i) {return d.value<simVal ? 0 : Math.pow(d.value, 2)})
		.on("tick", tick)
		.on("end", function() {
			//document.getElementById("togglelayout").click();
			document.getElementById("togglelayout").value = "Resume layout";
			bDynamicLayout = false;
		})
		.on("start", function() {
			//document.getElementById("togglelayout").click();
			document.getElementById("togglelayout").value = "Pause layout";
			bDynamicLayout = true;
		})	
		.start();
	force.nodes().forEach(function(d) {
		if(!(d.clr in clusterCenterNodes) || (d.weight > clusterCenterNodes[d.clr].weight)) {
			clusterCenterNodes[d.clr] = d;
		} 		
	});
	
	// radius of nodes depend on number of links they have
	gnode.append("circle")	
		.attr("class", "circle")   
		.attr("r", function(d) {
					w = force.links().filter(function(p){return p.source == d || p.target == d}).length;
					if(w < minRadius)
						w = minRadius;
					else if(w > maxRadius)
						w = maxRadius;
					return w; })
		//.style("fill", d3.rgb(142, 186, 229))
		.style("fill", function(d) { 
			return d.clr == -1 ? d3.rgb(255, 255, 255) : color(d.clr); 
			})
		.style("opacity", 0.9)
		.style("stroke", function(d) { 
			if(d.multicluster<=1)
				return node_stroke_clr;
			else
				return node_multicluster_stroke_clr;
			})
		.style("stroke-width", function(d) { 
			return d.multicluster; 
			});
		
	gnode.append("text")
		.attr("x", 12)
		.attr("dy", ".35em")
		.style("visibility", function(d) {
			return d.multicluster>1 ? "visible":"hidden";
		})
		.text(function(d) {
			return d.multicluster>1 ? "Multiple-"+d.title:d.title;
		});
	
	linkedByIndex = {};
	link.data().forEach(function(d) {
		linkedByIndex[d.source.index + "," + d.target.index] = 1;
	});	
	
	// create legend
	legend = svg.append("g")
		.attr("class","legend")
		.attr("transform","translate(50,30)")
		.style("font-size","12px")
		.call(d3.legend);
}
	
// called in FileChange()
// check if duplicate edges
function checkDuplicateEdge(e1, e2) {
	if((e1.source==e2.source && e1.target==e2.target) || (e1.source==e2.target && e1.target==e2.source))
		return true;
	else
		return false;
}

// called in init()
// and respond to selection form
// change to selected file, do some checks, save info, do filtering, and show?
function FileChange() {
	// get the filename selected
	var get_id = document.getElementById('data-chooser');
	var selval = get_id.options[get_id.selectedIndex].value;
	var fname;
	if(selval=="")
		fname = "";
	else
		fname = "data/"+selval+".json";
	// check if the selected file changes
	if(curFileName==fname) return;
	curFileName = fname;
	if(fname=="") 
		return;
	$("#data-desc").html("This network shows the data-to-data relationship in Harris surveys extracted from <a href=\"http://arc.irss.unc.edu/dvn/\">Odum Institute Dataverse Network</a> at UNC-Chapel Hill. A categorical data similarity measurement algorithm was used to extract a similarity adjancey matrix that was then used to create this data-to-data relationship graph. Each node represents a Harris survey data record; each edge links the pair of nodes based on their similarity measurement --- the darker the edge, the more similar the linked nodes.");
	// read corresponding json file
	d3.json(fname, function(error, data) {
		var i;
	    force.stop(); // stops the force auto positioning before changing data
		bDragOn = true;
		oriData = [];
		filterData = [];
		// description for each dataset.
		if ("description" in data) {
			var desctext = data.description;
			$("#data-desc").append("<br>");
			$("#data-desc").append("<b>" + selval + ":</b> ");
			$("#data-desc").append(desctext);
		}
		// check if duplicate edges and delete them
		// comment this out temporarily to speed up, there is no duplicate edges in sample connection file
		// for(i = data.links.length-1; i >= 0; i--){
			// for(var j=i-1; j>=0; j--) {
				// if(checkDuplicateEdge(data.links[i],data.links[j])) {
					// data.links.splice(i,1);
					// break;
				// }
			// }	
		// }
		
		// check group attribute to each node and save info in each node 
		// node.index_o: origin index for each node
		// node.value_sum: sum of link values related with the node
		// node.connections: number of links related with the node
		// node.clr: the first cluster number,  node.multicluster: how many clusters
		// node.gfirst: if the node is the first node in the cluster, 1; otherwise, 0
		// build group_represent object for all groups
		clusterLevel = 0;
		var connection_threshold = 0.5;
		var group_already = [];
		var group_represent = [];
		var node_index = 0;
		data.nodes.forEach(function (node) {
				node.index_o = node_index;
				node.value_sum = 0;
				node.connections = 0;
				
				// find all the related links for this node
				data.links.forEach(function(aryvalue){
					if ((aryvalue.source == node.index_o) || (aryvalue.target == node.index_o)) {
						node.value_sum += aryvalue.value;
						if (aryvalue.value >= connection_threshold) {
							node.connections += 1;
						}
					}
				});
				
				if(!node.group) {
					node.group = 0;
				}
				
				var grpStrArys = String(node.group).split(","); 
				node.clr = grpStrArys[0].trim();
				node.multicluster = grpStrArys.length;
				
				// node.gfirst, valuemax, and connectionmax are lists to same info on different level
				// for finding the first node in the group
				node.gfirst = []
				if (group_already.indexOf(node.clr) == -1) {
					node.gfirst[clusterLevel] = 1;
					group_already.push(node.clr); 
				} else {
					node.gfirst[clusterLevel] = 0;
				}
				
				var clrInt = parseInt(node.clr);
				if (!group_represent[clrInt]) {
					group_represent[clrInt] = {valuemax: {index_o: node.index_o, sum: node.value_sum},
											connectionmax: {index_o: node.index_o, sum: node.connections}
											};
				} else {
					// for finding value max node
					if (group_represent[clrInt]['valuemax']['sum'] < node.value_sum) {
						group_represent[clrInt]['valuemax'] = {index_o: node.index_o, sum: node.value_sum};
					}
					
					//for finding connection max node
					if (group_represent[clrInt]['connectionmax']['sum'] < node.connections) {
						group_represent[clrInt]['connectionmax'] = {index_o: node.index_o, sum: node.connections};
					}
				}
				node_index += 1;
		});
		// build array for valuemax nodes and connectionmax nodes
		var group_valuemax = [];
		var group_connectionmax = [];
		gLen = group_represent.length;
		for (i = 0; i < gLen; i++) {
			if (group_represent[i]) {
				group_valuemax[i] = group_represent[i]['valuemax']['index_o'];
				group_connectionmax[i] = group_represent[i]['connectionmax']['index_o'];
			}

		}
		
		// node.valuemax: if the node is the value max node in the cluster, 1; otherwise, 0
		// node.connectionmax: if the node has the max connection in the cluster, 1; otherwise, 0	
		data.nodes.forEach(function (node) {
				node.valuemax = []
				node.connectionmax = []
				
				if (group_valuemax.indexOf(node.index_o) != -1) {
					node.valuemax[clusterLevel] = 1; 
				} else {
					node.valuemax[clusterLevel] = 0;
				}
				if (group_connectionmax.indexOf(node.index_o) != -1) {
					node.connectionmax[clusterLevel] = 1; 
				} else {
					node.connectionmax[clusterLevel] = 0;
				}
				
				
				
		});
	
		// oriData is a list to save original data for each level
		oriData[clusterLevel] = {};
		oriData[clusterLevel]["nodes"] = JSON.parse(JSON.stringify(data.nodes));
		oriData[clusterLevel]["links"] = JSON.parse(JSON.stringify(data.links));
		// filter data
		// filter node data to get the representative node in the cluster
		var get_id = document.getElementById('RNode-chooser');
		RNodeVal = get_id.options[get_id.selectedIndex].value;
		existNodes = [];
		filterNodeData = data.nodes.filter(FilterByGRepresent);

		// filter link data to get links for representative nodes
		filterLineData = data.links.filter(function (aryvalue) {
			if ((existNodes.indexOf(aryvalue.source) != -1) && (existNodes.indexOf(aryvalue.target) != -1)) {
				aryvalue.source_o = aryvalue.source;
				aryvalue.source = existNodes.indexOf(aryvalue.source);
				aryvalue.target_o = aryvalue.target;
				aryvalue.target = existNodes.indexOf(aryvalue.target);
				return true;
			} else {
				return false;
			}
		});
				
		// filter link data based on selected similarity value
		filterData = filterLineData.filter(FilterBySimVal);
				
		SetupData();
        force.resume();
		bDragOn = false;
	});
}

// called from the onclick
// #RNode-chooser
// change for representative node selection
function RNodeChange() {
		// filter data
		// filter node data to get the representative node in the cluster
		var get_id = document.getElementById('RNode-chooser');
		RNodeVal = get_id.options[get_id.selectedIndex].value;
		
		existNodes = [];
		filterNodeData = oriData[clusterLevel].nodes.filter(FilterByGRepresent);
				
		// filter link data to get links for representative nodes
		filterLineData = oriData[clusterLevel].links.filter(function (aryvalue) {
			if ("source_o" in aryvalue) {
				aryvalue.source = aryvalue.source_o;
				aryvalue.target = aryvalue.target_o;
			}
			if ((existNodes.indexOf(aryvalue.source) != -1) && (existNodes.indexOf(aryvalue.target) != -1)) {
				aryvalue.source_o = aryvalue.source;
				aryvalue.source = existNodes.indexOf(aryvalue.source);
				aryvalue.target_o = aryvalue.target;
				aryvalue.target = existNodes.indexOf(aryvalue.target);
				return true;
			} else {
				return false;
			}
		});
				
		// filter link data based on selected similarity value
		filterData = filterLineData.filter(FilterBySimVal);
				
		SetupData();
        force.resume();
		bDragOn = false;
}

// called from onclick nodes
// function to show nodes in subclusters
function ShowNodesInCluster(ClusterNum) {
	if (subClusterOn) {
	subClusterOn = false;
	existNodes = [];
	// filter node data to get nodes in the selected cluster
	filterNodeData = oriData[clusterLevel].nodes.filter(function (aryvalue) {
		if (aryvalue.clr == ClusterNum) {
			existNodes.push(aryvalue.index_o); 
			if (aryvalue.subgroup.length >= clusterLevel+1) {
				subClusterOn = true;
			}
			return true;
		} else {
			return false;
		}
	});
	
	// filter link data to get links for nodes in the selected cluster
	filterLineData = oriData[clusterLevel].links.filter(function (aryvalue) {
		if ("source_o" in aryvalue) {
			aryvalue.source = aryvalue.source_o;
			aryvalue.target = aryvalue.target_o;
		}
		if ((existNodes.indexOf(aryvalue.source) != -1) && (existNodes.indexOf(aryvalue.target) != -1)) {
			aryvalue.source_o = aryvalue.source;
			aryvalue.source = existNodes.indexOf(aryvalue.source);
			aryvalue.target_o = aryvalue.target;
			aryvalue.target = existNodes.indexOf(aryvalue.target);
			return true;
		} else {
			return false;
		}
	});
	
	clusterLevel += 1;
	
	if (subClusterOn) {
		
		oriData[clusterLevel] = {}
		oriData[clusterLevel]["nodes"] = JSON.parse(JSON.stringify(filterNodeData));
		oriData[clusterLevel]["links"] = JSON.parse(JSON.stringify(filterLineData));
		
		
		oriData[clusterLevel].links.forEach(function(aryvalue){
			if ("source_o" in aryvalue) {
				delete aryvalue.source_o;
				delete aryvalue.target_o;
			}
		});
	
		var connection_threshold = 0.5;
		var group_already = [];
		var group_represent = [];
		var node_index = 0;
		oriData[clusterLevel].nodes.forEach(function (node) {
				node.index_o = node_index;
				
				node.clr = node.subgroup[clusterLevel-1];
				// for finding the first node in the group
				if (group_already.indexOf(node.clr) == -1) {
					node.gfirst[clusterLevel] = 1;
					group_already.push(node.clr); 
				} else {
					node.gfirst[clusterLevel] = 0;
				}
				
				var clrInt = parseInt(node.clr);
				if (!group_represent[node.clr]) {
					group_represent[node.clr] = {valuemax: {index_o: node.index_o, sum: node.value_sum},
											connectionmax: {index_o: node.index_o, sum: node.connections}
											};
				} else {
					// for finding value max node
					if (group_represent[node.clr]['valuemax']['sum'] < node.value_sum) {
						group_represent[node.clr]['valuemax'] = {index_o: node.index_o, sum: node.value_sum};
					}
					
					//for finding connection max node
					if (group_represent[node.clr]['connectionmax']['sum'] < node.connections) {
						group_represent[node.clr]['connectionmax'] = {index_o: node.index_o, sum: node.connections};
					}
				}
				node_index += 1;
		});
		// build array for valuemax nodes and connectionmax nodes
		var group_valuemax = [];
		var group_connectionmax = [];
		gLen = group_represent.length;
		for (i = 0; i < gLen; i++) {
			if (group_represent[i]) {
				group_valuemax[i] = group_represent[i]['valuemax']['index_o'];
				group_connectionmax[i] = group_represent[i]['connectionmax']['index_o'];
			}

		}
		
		// node.valuemax: if the node is the value max node in the cluster, 1; otherwise, 0
		// node.connectionmax: if the node has the max connection in the cluster, 1; otherwise, 0	
		oriData[clusterLevel].nodes.forEach(function (node) {
				
				if (group_valuemax.indexOf(node.index_o) != -1) {
					node.valuemax[clusterLevel] = 1; 
				} else {
					node.valuemax[clusterLevel] = 0;
				}
				if (group_connectionmax.indexOf(node.index_o) != -1) {
					node.connectionmax[clusterLevel] = 1; 
				} else {
					node.connectionmax[clusterLevel] = 0;
				}
								
		});
	
		// filter data
		// filter node data to get the representative node in the cluster
		existNodes = [];
		filterNodeData = oriData[clusterLevel].nodes.filter(FilterByGRepresent);

		// filter link data to get links for representative nodes
		filterLineData = oriData[clusterLevel].links.filter(function (aryvalue) {
			if ((existNodes.indexOf(aryvalue.source) != -1) && (existNodes.indexOf(aryvalue.target) != -1)) {
				aryvalue.source_o = aryvalue.source;
				aryvalue.source = existNodes.indexOf(aryvalue.source);
				aryvalue.target_o = aryvalue.target;
				aryvalue.target = existNodes.indexOf(aryvalue.target);
				return true;
			} else {
				return false;
			}
		});
					
	}
				
	// filter link data based on selected similarity value
	filterData = filterLineData.filter(FilterBySimVal);
				
	SetupData();
    force.resume();
	bDragOn = false;
	} else {
		alert("No more subclustering.")
	}
	
}

// called from contextmenu, click button
// function to go back the former level of clustering
function GoBackCluster() {
		
	if (clusterLevel > 0) {
		clusterLevel -= 1;
		existNodes = [];
		filterNodeData = oriData[clusterLevel].nodes.filter(FilterByGRepresent);

		// filter link data to get links for representative nodes
		filterLineData = oriData[clusterLevel].links.filter(function (aryvalue) {
			if ("source_o" in aryvalue) {
				aryvalue.source = aryvalue.source_o;
				aryvalue.target = aryvalue.target_o;
			}
			if ((existNodes.indexOf(aryvalue.source) != -1) && (existNodes.indexOf(aryvalue.target) != -1)) {
				aryvalue.source_o = aryvalue.source;
				aryvalue.source = existNodes.indexOf(aryvalue.source);
				aryvalue.target_o = aryvalue.target;
				aryvalue.target = existNodes.indexOf(aryvalue.target);
				return true;
			} else {
				return false;
			}
		});
				
		// filter link data based on selected similarity value
		filterData = filterLineData.filter(FilterBySimVal);
				
		SetupData();
        force.resume();
		bDragOn = false;
		
		subClusterOn = true;
	} else {
		alert("We are at the top level.")
	}
		
}

// ** Update data section (Called from the onclick)
// #sim-value
function SimValueChange() {
	var get_id = document.getElementById('sim-value');
	simVal = get_id.options[get_id.selectedIndex].value;
	filterData = filterLineData.filter(FilterBySimVal);
	link = SetupLink();
	
	node.call(force.drag)
		.call(node_drag);
	
	force.nodes(node.data())
		 .links(link.data())
		 .linkStrength(function(d, i) {return d.value<simVal ? 0 : Math.pow(d.value, 2)})
		 .start();	

	linkedByIndex = {};
	link.data().forEach(function(d) {
		linkedByIndex[d.source.index + "," + d.target.index] = 1;
	});		
}

// called from the onclick
// #button
function ResetView() {
	zoom.scale(1);
	zoom.translate([0, 0]);
	//svg.transition().duration(500).attr('transform', 'translate(' + zoom.translate() + ') scale(' + zoom.scale() + ')');
	svg.attr("transform", "translate(" + zoom.translate() + ")scale(" + zoom.scale() + ")");
}

// called from the onclick
// #togglelayout
function ToggleLayout(ref) {
	if(bDynamicLayout) {
		ref.value="Resume layout";
		bDynamicLayout = false;
		force.stop();
	}
	else {
		ref.value="Pause layout";
		bDynamicLayout = true;	
		force.resume();
	}
}

// called in updateSearch()
function RestoreNode(id) {
	var selectorstr = "g.node[id='"+id+"']"; 
	d3.select(selectorstr).select("text").transition()
		.duration(200)
		.style("visibility", "hidden");	
	d3.select(selectorstr).select("circle").transition()
		.duration(200)	
		.style("stroke", node_stroke_clr);	
}

// called in updateSearch()
function HgtNode(id) {
	var selectorstr = "g.node[id='"+id+"']";
	d3.select(selectorstr).select("text").transition()
		.duration(200)
		.style("visibility", "visible");	
	d3.select(selectorstr).select("circle").transition()
		.duration(200)	
		.style("stroke", d3.rgb(255, 0, 0));	
}

// called from the onkeyup
// #search
function updateSearch(querystr) {
	var k;
	if(querystr == "") {
		if(lastSearchNodes.length > 0) {
			for(k=0; k<lastSearchNodes.length; k++) 
				RestoreNode(lastSearchNodes[k]);
			lastSearchNodes.splice(0, lastSearchNodes.length);
		}
		lastSearchStr = "";
	}
	else if (querystr != lastSearchStr) {
		// clear out previously highlighted nodes
		if(lastSearchNodes.length > 0) {
			for(k=0; k<lastSearchNodes.length; k++) 
				RestoreNode(lastSearchNodes[k]);
			lastSearchNodes.splice(0, lastSearchNodes.length);
		}
		var partsOfStr = querystr.split(','); // comma is used for boolean or of different search terms
		var compoundstr = "";
		for(k=0; k<partsOfStr.length; k++) {
			partsOfStr[k].trim();
			if(partsOfStr[k]=="") continue;
			if(k==0) // last part
				compoundstr += partsOfStr[k];
			else
				compoundstr += "|" + partsOfStr[k];	
		}
		lastSearchStr = querystr;
		if(compoundstr == "") return;	
		var reqry = new RegExp(compoundstr, "i"); // do a case-insensitive and boolean or search match
	    filterNodeData.forEach(function(d, i) {
			if((d.title).match(reqry)) {
				var idstr = "N" + i;
				lastSearchNodes.push(idstr);
				HgtNode(idstr);
			}
		});	       
  	}		
}

(function(){
  try{
    var points = [
      {x:46.0, y:200.0, v:3950}, {x:77.8, y:194.8, v:3980}, {x:109.5, y:189.6, v:4010},
      {x:141.3, y:196.5, v:3970}, {x:173.0, y:184.4, v:4040}, {x:204.8, y:175.7, v:4090},
      {x:236.6, y:179.2, v:4070}, {x:268.3, y:168.8, v:4130}, {x:300.1, y:160.1, v:4180},
      {x:331.8, y:165.3, v:4150}, {x:363.6, y:153.2, v:4220}, {x:395.4, y:146.3, v:4260},
      {x:427.1, y:139.3, v:4300}, {x:458.9, y:142.8, v:4280}, {x:490.6, y:130.7, v:4350},
      {x:522.4, y:122.0, v:4400}, {x:554.2, y:125.5, v:4380}, {x:585.9, y:111.6, v:4460},
      {x:617.7, y:102.9, v:4510}, {x:649.4, y:106.4, v:4490}, {x:681.2, y:94.3, v:4560},
      {x:713.0, y:85.6, v:4610}, {x:744.7, y:78.7, v:4650}, {x:776.5, y:70.0, v:4700},
      {x:808.2, y:59.6, v:4760}, {x:840.0, y:50.6, v:4812}
    ];

    var svg = document.getElementById('valueChart');
    var hit = svg.querySelector('.chart-hit');
    var crosshair = document.getElementById('crosshair');
    var hoverDot = document.getElementById('hoverDot');
    var tooltip = document.getElementById('chartTooltip');
    var wrap = tooltip ? tooltip.parentElement : null;

    function nearestPoint(vbX){
      var best = points[0], bestDist = Infinity;
      for (var i = 0; i < points.length; i++){
        var d = Math.abs(points[i].x - vbX);
        if (d < bestDist){ bestDist = d; best = points[i]; }
      }
      return best;
    }

    function handleMove(evt){
      var rect = svg.getBoundingClientRect();
      if (!rect.width) return;
      var scaleX = 860 / rect.width;
      var scale = rect.width / 860;
      var vbX = (evt.clientX - rect.left) * scaleX;
      var p = nearestPoint(vbX);

      crosshair.setAttribute('x1', p.x);
      crosshair.setAttribute('x2', p.x);
      crosshair.style.opacity = 1;
      hoverDot.setAttribute('cx', p.x);
      hoverDot.setAttribute('cy', p.y);
      hoverDot.style.opacity = 1;

      if (tooltip){
        tooltip.textContent = '$' + p.v.toLocaleString();
        tooltip.style.left = (p.x * scale) + 'px';
        tooltip.style.top = (p.y * scale) + 'px';
        tooltip.hidden = false;
      }
    }

    function handleLeave(){
      crosshair.style.opacity = 0;
      hoverDot.style.opacity = 0;
      if (tooltip) tooltip.hidden = true;
    }

    if (hit){
      hit.addEventListener('mousemove', handleMove);
      hit.addEventListener('mouseleave', handleLeave);
      hit.addEventListener('touchmove', function(e){
        if (e.touches && e.touches[0]){
          handleMove({clientX: e.touches[0].clientX});
        }
      }, {passive:true});
      hit.addEventListener('touchend', handleLeave);
    }
  } catch(e){ /* chart hover is a progressive enhancement */ }
})();

(function(){
  try{
    var navLinks = document.querySelectorAll('.nav-links a[data-view]');
    var views = {
      collection: document.getElementById('view-collection'),
      marketplace: document.getElementById('view-marketplace'),
      insights: document.getElementById('view-insights')
    };

    function showView(name){
      Object.keys(views).forEach(function(key){
        if (!views[key]) return;
        views[key].hidden = (key !== name);
      });
      navLinks.forEach(function(link){
        link.classList.toggle('active', link.getAttribute('data-view') === name);
      });
    }

    navLinks.forEach(function(link){
      link.addEventListener('click', function(evt){
        evt.preventDefault();
        showView(link.getAttribute('data-view'));
        window.scrollTo({top:0, behavior:'smooth'});
      });
    });

    var pills = document.querySelectorAll('.cond-toggle .pill');
    pills.forEach(function(pill){
      pill.addEventListener('click', function(){
        pills.forEach(function(p){ p.classList.remove('active'); });
        pill.classList.add('active');
      });
    });
  } catch(e){ /* view switching is a progressive enhancement */ }
})();

(function(){
  try{
    var root = document.getElementById('accountMenuRoot');
    var btn = document.getElementById('accountBtn');
    var menu = document.getElementById('accountMenu');

    function closeMenu(){
      menu.hidden = true;
      root.classList.remove('open');
      btn.setAttribute('aria-expanded', 'false');
    }
    function openMenu(){
      menu.hidden = false;
      root.classList.add('open');
      btn.setAttribute('aria-expanded', 'true');
    }

    btn.addEventListener('click', function(evt){
      evt.stopPropagation();
      if (menu.hidden) openMenu(); else closeMenu();
    });
    document.addEventListener('click', function(evt){
      if (!root.contains(evt.target)) closeMenu();
    });
    document.addEventListener('keydown', function(evt){
      if (evt.key === 'Escape') closeMenu();
    });
  } catch(e){ /* account menu is a progressive enhancement */ }
})();

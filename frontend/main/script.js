// ===== Portfolio value chart hover (crosshair + tooltip on the line chart) =====
(function(){
  try{
    // Each point is one data value on the chart, already converted to pixel coordinates (x, y) plus its real dollar value (v).
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

    // Grab the chart elements we need to move/update while the user hovers.
    var svg = document.getElementById('valueChart');
    var hit = svg.querySelector('.chart-hit');
    var crosshair = document.getElementById('crosshair');
    var hoverDot = document.getElementById('hoverDot');
    var tooltip = document.getElementById('chartTooltip');
    var wrap = tooltip ? tooltip.parentElement : null;

    // Finds whichever data point's x-position is closest to the mouse, so the tooltip snaps to real values instead of following the cursor exactly.
    function nearestPoint(vbX){
      var best = points[0], bestDist = Infinity;
      for (var i = 0; i < points.length; i++){
        var d = Math.abs(points[i].x - vbX);
        if (d < bestDist){ bestDist = d; best = points[i]; }
      }
      return best;
    }

    // On mouse move, converts the cursor position into chart coordinates and updates the crosshair line, the highlighted dot, and the tooltip text/position.
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

    // When the mouse leaves the chart, hides the crosshair, dot, and tooltip again.
    function handleLeave(){
      crosshair.style.opacity = 0;
      hoverDot.style.opacity = 0;
      if (tooltip) tooltip.hidden = true;
    }

    // Wires up mouse and touch events on the invisible hit-area rectangle drawn over the chart.
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


// ===== Nav view switching (Collection / Marketplace / Insights) =====
(function(){
  try{
    // The three clickable nav links and the three page sections they each show.
    var navLinks = document.querySelectorAll('.nav-links a[data-view]');
    var views = {
      collection: document.getElementById('view-collection'),
      marketplace: document.getElementById('view-marketplace'),
      insights: document.getElementById('view-insights')
    };

    // Shows the chosen section and hides the other two, and highlights the matching nav link as active.
    function showView(name){
      Object.keys(views).forEach(function(key){
        if (!views[key]) return;
        views[key].hidden = (key !== name);
      });
      navLinks.forEach(function(link){
        link.classList.toggle('active', link.getAttribute('data-view') === name);
      });
    }

    // Clicking a nav link swaps the visible section instead of following the link, then scrolls back to the top.
    navLinks.forEach(function(link){
      link.addEventListener('click', function(evt){
        evt.preventDefault();
        showView(link.getAttribute('data-view'));
        window.scrollTo({top:0, behavior:'smooth'});
      });
    });

    // On the Marketplace page, clicking a New/Used pill marks it as the selected condition.
    var pills = document.querySelectorAll('.cond-toggle .pill');
    pills.forEach(function(pill){
      pill.addEventListener('click', function(){
        pills.forEach(function(p){ p.classList.remove('active'); });
        pill.classList.add('active');
      });
    });
  } catch(e){ /* view switching is a progressive enhancement */ }
})();


// ===== Account menu dropdown (top-right avatar) =====
(function(){
  try{
    // The dropdown's wrapper, its toggle button, and the menu panel itself.
    var root = document.getElementById('accountMenuRoot');
    var btn = document.getElementById('accountBtn');
    var menu = document.getElementById('accountMenu');

    // Hides the account menu and resets the button's open state.
    function closeMenu(){
      menu.hidden = true;
      root.classList.remove('open');
      btn.setAttribute('aria-expanded', 'false');
    }
    // Shows the account menu and marks the button as open (flips the little arrow).
    function openMenu(){
      menu.hidden = false;
      root.classList.add('open');
      btn.setAttribute('aria-expanded', 'true');
    }

    // Clicking the avatar button toggles the menu open or closed.
    btn.addEventListener('click', function(evt){
      evt.stopPropagation();
      if (menu.hidden) openMenu(); else closeMenu();
    });
    // Clicking anywhere outside the menu closes it.
    document.addEventListener('click', function(evt){
      if (!root.contains(evt.target)) closeMenu();
    });
    // Pressing Escape closes the menu too.
    document.addEventListener('keydown', function(evt){
      if (evt.key === 'Escape') closeMenu();
    });
  } catch(e){ /* account menu is a progressive enhancement */ }
})();


// ===== Load the real portfolio from the Spring Boot API =====
(function(){
  // If Spring Boot uses a port other than 8080, change this URL to match it.
  var API_BASE_URL = 'http://localhost:8080';

  function money(value){
    var number = Number(value);
    if (!Number.isFinite(number)) number = 0;
    return number.toLocaleString('en-US', { style: 'currency', currency: 'USD' });
  }
  function signedMoney(value){
    var number = Number(value);
    if (!Number.isFinite(number)) number = 0;
    return (number > 0 ? '+' : number < 0 ? '−' : '') + money(Math.abs(number));
  }
  function escapeHtml(value){
    return String(value == null ? '' : value).replace(/[&<>"']/g, function(ch){
      return ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'})[ch];
    });
  }
  function setStatus(message, state){
    var status = document.getElementById('portfolio-status');
    if (!status) return;
    status.textContent = message;
    status.className = 'portfolio-status' + (state ? ' ' + state : '');
  }
  function renderPortfolio(portfolio){
    var sets = Array.isArray(portfolio && portfolio.sets) ? portfolio.sets : [];
    var totalCost = Number(portfolio && portfolio.totalCost);
    var currentValue = Number(portfolio && portfolio.currentValue);
    var profit = Number(portfolio && portfolio.profit);

    if (!Number.isFinite(totalCost)) {
      totalCost = sets.reduce(function(sum, set){ return sum + (Number(set.purchasePrice) || 0); }, 0);
    }
    if (!Number.isFinite(currentValue)) {
      currentValue = sets.reduce(function(sum, set){ return sum + (Number(set.currentValue) || 0); }, 0);
    }
    if (!Number.isFinite(profit)) profit = currentValue - totalCost;
    var returnPercent = totalCost !== 0 ? (profit / totalCost) * 100 : 0;

    var valueNode = document.getElementById('portfolio-value');
    var costNode = document.getElementById('portfolio-cost');
    var countNode = document.getElementById('portfolio-set-count');
    var returnNode = document.getElementById('portfolio-return');
    var profitNode = document.getElementById('portfolio-profit');
    var viewAllNode = document.getElementById('portfolio-view-all');

    if (valueNode) valueNode.textContent = money(currentValue);
    if (costNode) costNode.textContent = 'Total cost: ' + money(totalCost);
    if (countNode) countNode.textContent = String(sets.length);
    if (returnNode) {
      returnNode.textContent = (returnPercent > 0 ? '+' : '') + returnPercent.toFixed(1) + '%';
      returnNode.classList.toggle('negative', returnPercent < 0);
    }
    if (profitNode) profitNode.textContent = (profit >= 0 ? 'Gain: ' : 'Loss: ') + money(Math.abs(profit));
    if (viewAllNode) viewAllNode.textContent = 'View all ' + sets.length + ' →';

    var sorted = sets.slice().sort(function(a, b){
      return (Number(b.currentValue) || 0) - (Number(a.currentValue) || 0);
    });
    var holdings = document.getElementById('portfolio-holdings');
    var grid = document.getElementById('portfolio-grid');

    if (holdings) {
      holdings.innerHTML = sorted.length ? sorted.slice(0, 4).map(function(set, index){
        var setProfit = (Number(set.currentValue) || 0) - (Number(set.purchasePrice) || 0);
        var deltaClass = setProfit >= 0 ? 'up' : 'down';
        var arrow = setProfit >= 0 ? '▲' : '▼';
        var colors = ['var(--red)', 'var(--blue)', 'var(--green)', 'var(--yellow)'];
        return '<div class="row">' +
          '<div class="set-name"><span class="swatch" style="background:' + colors[index % colors.length] + '"></span>' +
          escapeHtml(set.setNumber) + ' ' + escapeHtml(set.name) + '</div>' +
          '<div class="row-right"><span class="price">' + money(set.currentValue) + '</span>' +
          '<span class="delta ' + deltaClass + '">' + arrow + ' ' + signedMoney(setProfit) + ' profit</span></div>' +
          '</div>';
      }).join('') : '<p class="portfolio-empty">No sets in your portfolio yet.</p>';
    }
    if (grid) {
      grid.innerHTML = sorted.length ? sorted.map(function(set){
        var setProfit = (Number(set.currentValue) || 0) - (Number(set.purchasePrice) || 0);
        var deltaClass = setProfit >= 0 ? 'up' : 'down';
        var arrow = setProfit >= 0 ? '▲' : '▼';
        return '<div class="card">' +
          '<div class="card-top"><span class="stud"></span><span class="stud"></span><span class="stud"></span></div>' +
          '<div class="card-body"><div class="name">' + escapeHtml(set.setNumber) + ' ' + escapeHtml(set.name) + '</div>' +
          '<div class="meta"><span class="price">' + money(set.currentValue) + '</span>' +
          '<span class="delta ' + deltaClass + '">' + arrow + ' ' + signedMoney(setProfit) + '</span></div>' +
          '<div class="meta"><span class="source-meta">Purchase price: ' + money(set.purchasePrice) + '</span></div>' +
          '</div></div>';
      }).join('') : '<p class="portfolio-empty">No sets in your portfolio yet. Add a set through the API to see it here.</p>';
    }
    setStatus('Portfolio loaded from the backend.', 'success');
  }
  function loadPortfolio(){
    setStatus('Connecting to your portfolio…');
    fetch(API_BASE_URL + '/api/portfolio')
      .then(function(response){
        if (!response.ok) throw new Error('The API returned HTTP ' + response.status + '.');
        return response.json();
      })
      .then(renderPortfolio)
      .catch(function(error){
        setStatus('Could not load portfolio. Make sure the Spring Boot backend is running at ' +
          API_BASE_URL + ' and that /api/portfolio is available. Details: ' + error.message, 'error');
        var holdings = document.getElementById('portfolio-holdings');
        var grid = document.getElementById('portfolio-grid');
        if (holdings) holdings.innerHTML = '<p class="portfolio-empty">Portfolio data is unavailable. Check that the backend is running.</p>';
        if (grid) grid.innerHTML = '';
      });
  }
  
  // ===== Add a set to the portfolio =====
  var addSetButton = document.getElementById('add-set-button');
  var addSetDialog = document.getElementById('add-set-dialog');
  var addSetForm = document.getElementById('add-set-form');
  var cancelAddSet = document.getElementById('cancel-add-set');
  var addSetMessage = document.getElementById('add-set-message');
  var submitAddSet = document.getElementById('submit-add-set');

  // Open the form when the user clicks "+ Add a Set".
  if (addSetButton && addSetDialog) {
    addSetButton.addEventListener('click', function () {
      addSetMessage.textContent = '';
      addSetDialog.showModal();
    });
  }

  // Close the form when Cancel is clicked.
  if (cancelAddSet && addSetDialog) {
    cancelAddSet.addEventListener('click', function () {
      addSetDialog.close();
    });
  }

  // Send the new set to Spring Boot.
  if (addSetForm) {
    addSetForm.addEventListener('submit', function (event) {
      event.preventDefault();

      var formData = new FormData(addSetForm);

      var newSet = {
        setNumber: formData.get('setNumber').trim(),
        purchasePrice: Number(formData.get('purchasePrice')),
        condition: formData.get('condition'),
      };

      addSetMessage.textContent = 'Adding set…';
      submitAddSet.disabled = true;

      fetch(API_BASE_URL + '/api/sets', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(newSet)
      })
        .then(function (response) {
          if (!response.ok) {
            throw new Error('The API returned HTTP ' + response.status + '.');
          }

          // The backend may return an empty response, so don't parse JSON here.
          addSetDialog.close();
          addSetForm.reset();

          setStatus('Set added! Refreshing your portfolio…');
          loadPortfolio();
        })
        .catch(function (error) {
          addSetMessage.textContent =
            'Could not add the set. ' + error.message;
        })
        .finally(function () {
          submitAddSet.disabled = false;
        });
    });
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', loadPortfolio);
  } else {
    loadPortfolio();
  }
})();

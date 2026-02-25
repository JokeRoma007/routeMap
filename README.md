The application is used to create and manage transport routes with regard to the type of cargo being transported. It allows you to enter locations (points) with attributes, sort them by priority (dry, refrigerated, frozen), and save completed routes. The application is built on Jetpack Compose and uses tabs for a clear division of functions.

Main screen and tabs
The top bar contains the name of the application, and below it are four tabs:

1. Points – entering and managing a list of points to visit.
2. Route – setting the starting point, calculating the order, and displaying the resulting route.
3. Favorite points – a list of previously marked points that can be quickly added to the current list.
4. Saved routes – an overview of previously saved routes with the option to restore, rename, or delete them.

 <img width="611" height="545" alt="Знімок екрана 2026-02-25 164052" src="https://github.com/user-attachments/assets/f2f8fede-aa2d-4cb5-9424-a826396f6de6" />


 Detailed description of functions

 1. "Points" tab
- Adding a new point: the form contains the following fields:
  - Name (required)
- Latitude (decimal number, required)
- Longitude (decimal number, required)
- Type of cargo (selection from a drop-down list: dry, refrigerated, frozen)
  - Number of pallets (integer, optional, default 0)
  - Address (text, optional)
  After pressing the "Add" button, the point is saved to the list and the form is cleared. If any mandatory item is missing or the coordinates are not valid numbers, a notification (Toast) is displayed.
- List of added points: each point is displayed as a card. Clicking on the card expands it and allows you to edit all data (name, coordinates, type, pallets, address). Changes are immediately reflected in the list.
- Each card has a star icon – click on it to add the point to your favorites. If it is already in your favorites, the app will notify you.
- With each change (addition, modification), the route is automatically recalculated if it has already been constructed (response to input changes).


<img width="618" height="455" alt="Знімок екрана 2026-02-25 164216" src="https://github.com/user-attachments/assets/f857ec58-45ff-4c5f-bd15-35aa34040202" />


2. "Route" tab
- Input for starting coordinates: two text fields – starting latitude and longitude.
- The "Build Route" button starts calculating the order of points. First, it checks whether valid starting coordinates have been entered and whether there are any points to sort. If not, it displays an error message.
- Sorting algorithm:
  - Points are sorted by cargo type in a fixed order: dry → refrigerated → frozen.
  - Within each group, the algorithm proceeds as follows: from the current position (first the starting point, then gradually the last visited point), it selects the nearest point of the given type that has not yet been visited. The distance is calculated as the Euclidean distance (for simplicity, the curvature of the Earth is not taken into account).
- The result is a list of points in the order in which they are to be visited.
- After calculation, a list of points in this order is displayed under the button.
- Automatic saving: immediately after successfully building the route, a dialog box opens for entering the route name (pre-filled with "Маршрут X", where X is the number according to the number of routes saved so far). The user can edit the name and save the route to the list of saved routes by confirming. If the dialog box is canceled, the route is not saved.
- If the user later changes the points in the "Points" tab (adds, edits, deletes), the route is automatically recalculated (if it was created) and displayed again.

<img width="613" height="623" alt="Знімок екрана 2026-02-25 164143" src="https://github.com/user-attachments/assets/e38352b3-cadc-44c4-bd14-a19e2dc5a744" />


3. "Favorite Points" tab
- Displays a list of all points that the user has previously marked as favorites (using the star in the Points tab).
- There are two icons for each point:
  - "+" – adds this point to the current list of points (in the Points tab) and automatically recalculates the route, if any.
  - Empty star – removes the point from favorites.
- This tab is used for quick reuse of frequently visited places.

  <img width="618" height="611" alt="Знімок екрана 2026-02-25 164151" src="https://github.com/user-attachments/assets/97801d13-e0e0-4ace-ab35-4a2ae88860f0" />


 4. "Saved Routes" tab
- List of previously saved routes. Each item contains the route name and number of points.
- There are three actions (icons) for each route:
- Refresh (refresh icon) – loads the route points into the editor (Points tab) and the start coordinates (the first point of the route is set as the start), switches to the Route tab, and displays the list of points in the order in which they were saved.
  - Rename (pencil icon) – allows you to change the name of the route directly in the list. After clicking, a text field and buttons for saving (check mark) or canceling (cross) are displayed. When saving, the system checks that the name is not empty.
  - Delete (trash can icon) – removes the route from the list.
 
<img width="618" height="475" alt="Знімок екрана 2026-02-25 164204" src="https://github.com/user-attachments/assets/889f2cbe-e236-4793-8ba1-69b59643bea7" />


Other features
- Input validation: All numeric fields are checked for correct format; invalid values are ignored or trigger a warning.
- User feedback: Important actions (adding a point, saving a route, error) are accompanied by short toast messages.
- Automatic route recalculation: Any change in the list of points (addition, modification, deletion) triggers a redrawing of the route if it has already been constructed once. This ensures that the user sees the current order according to the latest changes.
- Simple distance calculation: Euclidean distance (without taking into account the curvature of the Earth) is used for planning purposes. This is sufficient for smaller areas or for demonstration purposes.


It is suitable for drivers, dispatchers, or anyone who needs to plan visits to multiple locations with different cargo temperature requirements and optimize the order based on the current location.

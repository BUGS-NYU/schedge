import React from "react";
import Link from "next/link";
import WishlistCourse from "components/WishlistCourse";
import { usePageState } from "components/state";
import styles from "./schedule.module.css";
import Calendar from "components/Calendar";
import ScheduleCourse from "components/ScheduleCourse";
import { dayToStr } from "components/constants";
import create from "zustand";

const useSchedule = create((set, get) => {
  const addToScheduled = (course) => {
    const { schedule } = get();
    set({ schedule: [...schedule, course.registrationNumber] });
  };

  const addToWishlist = (course) => {
    const { wishlist } = get();
    set({ wishlist: [...wishlist, course.registrationNumber] });
  };

  return {
    schedule: [],
    wishlist: [],

    addToScheduled,
    addToWishlist,
  };
});

function SchedulePage({ scheduled, toggleCourseSelect, clearSchedule }) {
  const { year, semester } = usePageState();

  const { schedule } = useSchedule();
  const [checkboxes, setCheckboxes] = React.useState(
    //JSON.parse(window.localStorage.getItem(`${year}-${semester}-checkbox-state`)) || {}
    {}
  );
  const [wishlist, setWishlist] = React.useState([]);

  /*
  React.useEffect(() => {
    (async () => {
      try {
        const localStorage = new localStorageContainer();
        const wishlist = localStorage.getState("wishlist");
        setWishlist(wishlist);
        setLocalStorage(localStorage);
        //Empty wishlist and schedule so clear schedule state
        if (wishlist.length === 0) {
          setSchedule({});
          return;
        }
        if (scheduled.length === 0) {
          setSchedule({});
          return;
        }
        //Make request to API to check schedule validity
        const response = await fetch(
          `https://schedge.a1liu.com/${year}/${semester}/generateSchedule?registrationNumbers=${scheduled
            .map((course) => course.courseRegistrationNumber)
            .join(",")}`
        );

//handle invalid data
        if (!response.ok) {
          return;
        }
        const data = await response.json();
//if not valid, make toast visible and clear checkboxes
        if (!data.valid) {
          setToast({
            open: true,
            message: `${data.conflictA.sectionName} & ${data.conflictB.sectionName} conflicts with one another!`, //make message more meaningful
            horizontal: "center",
            vertical: "top",
          });
//Remove both conflicted course
          let newCheckboxes = { ...checkboxes };
          newCheckboxes[data.conflictA.registrationNumber] = false;
          newCheckboxes[data.conflictB.registrationNumber] = false;
          setCheckboxes(newCheckboxes);
          localStorage.setItem(
            `${year}-${semester}-checkbox-state`,
            JSON.stringify(newCheckboxes)
          );
          toggleCourseSelect({
            year,
            semester,
            conflicts: [
              { courseRegistrationNumber: data.conflictA.registrationNumber },
              { courseRegistrationNumber: data.conflictB.registrationNumber },
            ],
          });
        } else {
          setSchedule(data);
        }
//handle layout data from schedge
      } catch (error) {
        console.error(error);
      }
    })();
  }, [
    year,
    semester,
    scheduled,
    checkboxes,
    toggleCourseSelect,
    wishlist.length,
  ]);
  */

  const handleClearSchedule = () => {
    setCheckboxes({});
    localStorage.setItem(
      `${year}-${semester}-checkbox-state`,
      JSON.stringify({})
    );
  };

  // const handleOnClose = () => {
  //   setToast({
  //     open: false,
  //     message: "",
  //     horizontal: "center",
  //     vertical: "top",
  //   });
  // };

  const removeCourse = (course) => {
    const courses = localStorage
      .getState("wishlist")
      .filter((wishlistCourse) => {
        return wishlistCourse.registrationNumber !== course.registrationNumber;
      });
    localStorage.saveState({ wishlist: courses });
    setWishlist(courses);

    /*if (
      checkboxes[course.registrationNumber] !== undefined &&
      checkboxes[course.registrationNumber]
    ) {
      toggleCourseSelect({
        year,
        semester,
        courseRegistrationNumber: course.registrationNumber,
      });
    }
    let newCheckboxes = { ...checkboxes };
    newCheckboxes[course.registrationNumber] = false;
    setCheckboxes(newCheckboxes);
    localStorage.setItem(
      `${year}-${semester}-checkbox-state`,
      JSON.stringify(newCheckboxes)
    );*/
  };

  const handleOnChange = (event, course, checkbox) => {
    if (event.target.checked) {
      if (!scheduled.includes(course.registrationNumber)) {
        toggleCourseSelect({
          year,
          semester,
          courseRegistrationNumber: course.registrationNumber,
        });
      }
    } else {
      toggleCourseSelect({
        year,
        semester,
        courseRegistrationNumber: course.registrationNumber,
      });
    }
    let newCheckboxes = { ...checkboxes };
    newCheckboxes[checkbox] = event.target.checked;
    setCheckboxes(newCheckboxes);
    localStorage.setItem(
      `${year}-${semester}-checkbox-state`,
      JSON.stringify(newCheckboxes)
    );
  };

  const _renderCourses = (dayNum) =>
    schedule[dayToStr[dayNum]] !== undefined &&
    Object.values(schedule[dayToStr[dayNum]]).map((course, i) => {
      return <ScheduleCourse course={course} key={i} />;
    });

  return (
    <div className={styles.container}>
      <Calendar renderCourses={_renderCourses} />
      <div
        style={{
          marginTop: "2rem",
        }}
      >
        <div className={styles.header}>
          <h2 className={styles.wishlist}>{`Wishlist (${wishlist.length})`}</h2>
        </div>

        <div className={styles.wishlistCoursesList}>
          {wishlist.length === 0 ? (
            <div className={styles.emptyWishlistContainer}>
              Your wishlist appears empty!
              <Link
                href={{
                  pathname: "/",
                }}
              >
                <a style={{ textDecoration: "none", color: "purpleLight" }}>
                  Search
                </a>
              </Link>
              for courses to add to your wishlist
            </div>
          ) : (
            wishlist.map((course, i) => {
              return (
                <WishlistCourse
                  key={i}
                  year={year}
                  semester={semester}
                  course={course}
                  checkboxes={checkboxes}
                  removeCourse={removeCourse}
                  handleOnChange={handleOnChange}
                />
              );
            })
          )}
        </div>

        <button
          onClick={handleClearSchedule}
          className={styles.clearScheduleButton}
          onKeyPress={() => clearSchedule({ year, semester })}
          tabIndex={0}
        >
          Clear Schedule
        </button>
      </div>
    </div>
  );
}

export default SchedulePage;

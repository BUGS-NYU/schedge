import React from "react";
import Link from 'next/link';
import styled from "styled-components";
import WishlistCourse from "components/WishlistCourse";
import Calendar from "components/Calendar";
import ScheduleCourse from "components/ScheduleCourse";
import { dayToStr } from "components/constants";
import localStorageContainer from "components/localStorage";

function SchedulePage({
  year,
  semester,
  scheduled,
  toggleCourseSelect,
  clearSchedule,
}) {
  const [schedule, setSchedule] = React.useState({});
  const [checkboxes, setCheckboxes] = React.useState(
    //JSON.parse(window.localStorage.getItem(`${year}-${semester}-checkbox-state`)) || {}
    {}
  );
  const [wishlist, setWishlist] = React.useState([]);
  const [localStorage, setLocalStorage] = React.useState(null);
  const [Toast, setToast] = React.useState({
    open: false,
    message: "",
    horizontal: "center",
    vertical: "top",
  });
  const { open, message, horizontal, vertical } = Toast;

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

  const handleClearSchedule = () => {
    setCheckboxes({});
    localStorage.setItem(
      `${year}-${semester}-checkbox-state`,
      JSON.stringify({})
    );
  };

  const handleOnClose = () => {
    setToast({
      open: false,
      message: "",
      horizontal: "center",
      vertical: "top",
    });
  };

  const removeCourse = (course) => {
    const courses = localStorage.getState("wishlist").filter((wishlistCourse) => {
      return wishlistCourse.registrationNumber !== course.registrationNumber;
    });
    localStorage.saveState({ wishlist: courses});
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
    <Container>
      <Calendar renderCourses={_renderCourses} />
      <div
        style={{
          marginTop: "2rem",
        }}
      >
        <Header>
          <h2 className="wishlist">{`Wishlist (${wishlist.length})`}</h2>
        </Header>
        <WishlistCoursesList>
          {wishlist.length === 0 ? (
            <EmptyWishlistContainer>
              Your wishlist appears empty!
              <Link
                href={{
                  pathname: "/",
                }}
                style={{ textDecoration: "none", color: "purpleLight" }}
              >
                Search
              </Link>
              for courses to add to your wishlist
            </EmptyWishlistContainer>
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
        </WishlistCoursesList>
        <ClearScheduleButton
          onClick={handleClearSchedule}
          onKeyPress={() => clearSchedule({ year, semester })}
          role="button"
          tabIndex={0}
        >
          Clear Schedule
        </ClearScheduleButton>
      </div>
    </Container>
  );
}

const Container = styled.div`
  padding: 2rem 5vw;
  display: flex;
  justify-content: center;
`;

const Header = styled.div`
  display: flex;
  width: 100%;
  height: 3rem;
  background-color: var(--purpleMain);
  align-items: center;
  justify-content: center;

  & > .wishlist {
    font-size: 1rem;
    color: var(--grey200);
  }
`;

const WishlistCoursesList = styled.div`
  height: 100vh;
  width: 20rem;
  background-color: var(--grey200);
  overflow: scroll;
`;

const ClearScheduleButton = styled.div`
  width: 50%;
  cursor: pointer;
  margin-top: 1rem;
  color: #bd2f2f;
  border-radius: 6px;
  border: 3px solid #bd2f2f;
  text-align: center;
  padding: 8px 16px;
`;

const EmptyWishlistContainer = styled.div`
  color: var(--grey800);
  padding: 10px;
`;

export default SchedulePage;

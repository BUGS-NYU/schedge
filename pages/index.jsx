import Head from "next/head";
import Image from "next/image";
import { DebugRender } from "components/debug";

const Home = () => {
  return (
    <div>
      <DebugRender title={"Hello World"} deps={[]}></DebugRender>
    </div>
  );
};

export default Home;
